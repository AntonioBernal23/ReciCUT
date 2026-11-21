package com.report_service.service;

import com.report_service.entity.*;
import com.report_service.events.CollectionCancelledEvent;
import com.report_service.events.CollectionCompletedEvent;
import com.report_service.events.UserRegisteredEvent;
import com.report_service.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * El lado de ESCRITURA del modelo de lectura (la "C" de CQRS).
 *
 * Solo lo llaman los @KafkaListener. Nunca se expone por HTTP: los datos de
 * reportes entran UNICAMENTE por eventos.
 */
@Service
public class ReportIngestionService {

    private static final Logger log = LoggerFactory.getLogger(ReportIngestionService.class);

    private final CollectionFactRepository factRepo;
    private final DailyMaterialSummaryRepository dailyRepo;
    private final UserStatsRepository userStatsRepo;
    private final CenterStatsRepository centerStatsRepo;
    private final ImpactCalculator impacto;

    public ReportIngestionService(CollectionFactRepository factRepo,
                                  DailyMaterialSummaryRepository dailyRepo,
                                  UserStatsRepository userStatsRepo,
                                  CenterStatsRepository centerStatsRepo,
                                  ImpactCalculator impacto) {
        this.factRepo = factRepo;
        this.dailyRepo = dailyRepo;
        this.userStatsRepo = userStatsRepo;
        this.centerStatsRepo = centerStatsRepo;
        this.impacto = impacto;
    }

    /**
     * Procesa una recoleccion completada.
     *
     * @Transactional abarca TODO: si falla a la mitad, no queda un hecho
     * guardado con los agregados sin actualizar. Sin la transaccion, un error
     * intermedio dejaria tus numeros inconsistentes para siempre.
     */
    @Transactional
    public void ingerirCompletada(CollectionCompletedEvent e) {

        // ---------- DEFENSA DE IDEMPOTENCIA ----------
        // Kafka garantiza "al menos una entrega": el mismo evento puede
        // llegar dos veces (un reinicio, un rebalanceo del grupo).
        // Sin esto, los kilos se contarian doble y NADIE se daria cuenta.
        if (factRepo.existsByCollectionId(e.collectionId())) {
            log.info("Evento duplicado de la recoleccion {}, se ignora", e.collectionId());
            return;
        }

        LocalDate dia = e.completedAt().toLocalDate();
        BigDecimal co2Total = BigDecimal.ZERO;

        CollectionFactEntity hecho = CollectionFactEntity.builder()
                .collectionId(e.collectionId())
                .requesterId(e.requesterId())
                .collectorId(e.collectorId())
                .recyclingCenterId(e.recyclingCenterId())
                .completedAt(e.completedAt())
                .completedDate(dia)
                .totalWeightKg(e.totalWeightKg() == null ? BigDecimal.ZERO : e.totalWeightKg())
                .itemCount(e.items() == null ? 0 : e.items().size())
                .ingestedAt(LocalDateTime.now())
                .co2AvoidedKg(BigDecimal.ZERO)
                .build();

        if (e.items() != null) {
            for (CollectionCompletedEvent.Item item : e.items()) {
                BigDecimal co2 = impacto.co2De(item.materialType(), item.weightKg());
                co2Total = co2Total.add(co2);

                hecho.addMaterial(CollectionMaterialFactEntity.builder()
                        .collectionId(e.collectionId())
                        .materialType(item.materialType())
                        .weightKg(item.weightKg())
                        .co2AvoidedKg(co2)
                        .completedDate(dia)
                        .build());

                // Agregado diario: suma atomica (ver el UPSERT del repositorio)
                dailyRepo.acumular(dia, item.materialType().name(), item.weightKg(), co2);
            }
        }

        hecho.setCo2AvoidedKg(co2Total);
        factRepo.save(hecho);

        acumularUsuario(e, co2Total);
        acumularCentro(e, co2Total);

        log.info("Recoleccion {} ingerida: {} kg, {} kg de CO2 evitado",
                e.collectionId(), hecho.getTotalWeightKg(), co2Total);
    }

    @Transactional
    public void ingerirCancelada(CollectionCancelledEvent e) {
        UserStatsEntity u = userStatsRepo.findById(e.requesterId())
                .orElseGet(() -> UserStatsEntity.builder().userId(e.requesterId()).build());
        u.setCancelledCollections(u.getCancelledCollections() + 1);
        userStatsRepo.save(u);
    }

    /**
     * Guarda el nombre del usuario para el ranking.
     *
     * Esto es desnormalizacion a proposito: el nombre "pertenece" a
     * user-service, pero tenerlo aqui evita 10 llamadas HTTP por cada top-10.
     */
    @Transactional
    public void ingerirUsuarioRegistrado(UserRegisteredEvent e) {
        String nombre = ((e.name() == null ? "" : e.name()) + " " +
                         (e.lastName() == null ? "" : e.lastName())).trim();

        UserStatsEntity u = userStatsRepo.findById(e.userId())
                .orElseGet(() -> UserStatsEntity.builder().userId(e.userId()).build());
        u.setDisplayName(nombre.isEmpty() ? ("Usuario " + e.userId()) : nombre);
        userStatsRepo.save(u);
    }

    // ---------------------------------------------------------------

    private void acumularUsuario(CollectionCompletedEvent e, BigDecimal co2) {
        UserStatsEntity u = userStatsRepo.findById(e.requesterId())
                .orElseGet(() -> UserStatsEntity.builder().userId(e.requesterId()).build());

        u.setTotalCollections(u.getTotalCollections() + 1);
        u.setTotalWeightKg(u.getTotalWeightKg().add(
                e.totalWeightKg() == null ? BigDecimal.ZERO : e.totalWeightKg()));
        u.setCo2AvoidedKg(u.getCo2AvoidedKg().add(co2));
        if (u.getFirstCollectionAt() == null) {
            u.setFirstCollectionAt(e.completedAt());
        }
        u.setLastCollectionAt(e.completedAt());
        userStatsRepo.save(u);
    }

    private void acumularCentro(CollectionCompletedEvent e, BigDecimal co2) {
        if (e.recyclingCenterId() == null) return;

        CenterStatsEntity c = centerStatsRepo.findById(e.recyclingCenterId())
                .orElseGet(() -> CenterStatsEntity.builder()
                        .centerId(e.recyclingCenterId()).build());

        c.setTotalCollections(c.getTotalCollections() + 1);
        c.setTotalWeightKg(c.getTotalWeightKg().add(
                e.totalWeightKg() == null ? BigDecimal.ZERO : e.totalWeightKg()));
        c.setCo2AvoidedKg(c.getCo2AvoidedKg().add(co2));
        c.setLastCollectionAt(e.completedAt());
        centerStatsRepo.save(c);
    }
}
