package com.report_service.service;

import com.report_service.dto.*;
import com.report_service.enums.Granularity;
import com.report_service.projection.MaterialAggregate;
import com.report_service.projection.TimelinePoint;
import com.report_service.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * El lado de LECTURA (la "Q" de CQRS).
 *
 * Consulta UNICAMENTE las tablas de este servicio. No llama a
 * collection-service ni a user-service ni a nadie. Por eso responde en
 * milisegundos y sigue funcionando aunque los demas esten caidos.
 */
@Service
public class ReportQueryService {

    private final CollectionFactRepository factRepo;
    private final CollectionMaterialFactRepository materialRepo;
    private final UserStatsRepository userStatsRepo;
    private final CenterStatsRepository centerStatsRepo;
    private final EquivalenceCalculator equivalencias;

    public ReportQueryService(CollectionFactRepository factRepo,
                              CollectionMaterialFactRepository materialRepo,
                              UserStatsRepository userStatsRepo,
                              CenterStatsRepository centerStatsRepo,
                              EquivalenceCalculator equivalencias) {
        this.factRepo = factRepo;
        this.materialRepo = materialRepo;
        this.userStatsRepo = userStatsRepo;
        this.centerStatsRepo = centerStatsRepo;
        this.equivalencias = equivalencias;
    }

    @Transactional(readOnly = true)
    public SummaryReportDTO resumen(LocalDate desde, LocalDate hasta) {
        long recolecciones = factRepo.countByCompletedDateBetween(desde, hasta);
        BigDecimal peso = nvl(factRepo.sumPeso(desde, hasta));
        BigDecimal co2  = nvl(factRepo.sumCo2(desde, hasta));

        long canceladas = userStatsRepo.findAll().stream()
                .mapToInt(u -> u.getCancelledCollections()).sum();

        BigDecimal promedio = recolecciones == 0
                ? BigDecimal.ZERO
                : peso.divide(BigDecimal.valueOf(recolecciones), 3, RoundingMode.HALF_UP);

        return new SummaryReportDTO(
                new PeriodDTO(desde, hasta),
                recolecciones,
                canceladas,
                peso,
                factRepo.contarUsuariosActivos(desde, hasta),
                factRepo.contarRecolectoresActivos(desde, hasta),
                promedio,
                equivalencias.construir(co2, desde, hasta)
        );
    }

    @Transactional(readOnly = true)
    public MaterialBreakdownDTO porMaterial(LocalDate desde, LocalDate hasta) {
        List<MaterialAggregate> filas = materialRepo.agregarPorMaterial(desde, hasta);

        BigDecimal total = filas.stream()
                .map(f -> nvl(f.getTotalWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<MaterialBreakdownDTO.MaterialLine> lineas = filas.stream()
                .map(f -> new MaterialBreakdownDTO.MaterialLine(
                        f.getMaterialType(),
                        nvl(f.getTotalWeight()),
                        porcentaje(nvl(f.getTotalWeight()), total),
                        f.getCollectionCount() == null ? 0 : f.getCollectionCount(),
                        nvl(f.getTotalCo2())))
                .toList();

        return new MaterialBreakdownDTO(new PeriodDTO(desde, hasta), total, lineas);
    }

    /**
     * Serie temporal, RELLENANDO los dias vacios.
     *
     * Si solo devolvieras lo que salio del GROUP BY, los dias sin
     * recolecciones desapareceria y la grafica del frontend quedaria
     * deformada: parecerian dias consecutivos cuando hay huecos.
     * Es una linea de codigo y la diferencia entre una grafica correcta y
     * una que miente.
     */
    @Transactional(readOnly = true)
    public TimelineReportDTO linea(LocalDate desde, LocalDate hasta, Granularity granularidad) {
        Map<LocalDate, TimelinePoint> porDia = new HashMap<>();
        for (TimelinePoint p : factRepo.serieDiaria(desde, hasta)) {
            porDia.put(p.getDate(), p);
        }

        List<TimelineReportDTO.Point> puntos = new ArrayList<>();
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            TimelinePoint p = porDia.get(d);
            puntos.add(new TimelineReportDTO.Point(
                    d,
                    p == null ? BigDecimal.ZERO : nvl(p.getWeightKg()),
                    p == null || p.getCollectionCount() == null ? 0 : p.getCollectionCount()));
        }

        return new TimelineReportDTO(
                granularidad == null ? Granularity.DAY : granularidad,
                new PeriodDTO(desde, hasta), puntos);
    }

    @Transactional(readOnly = true)
    public EnvironmentalImpactDTO impacto(LocalDate desde, LocalDate hasta) {
        return equivalencias.construir(nvl(factRepo.sumCo2(desde, hasta)), desde, hasta);
    }

    @Transactional(readOnly = true)
    public LeaderboardDTO ranking(int limite) {
        List<LeaderboardDTO.Entry> entradas = new ArrayList<>();
        int puesto = 1;
        for (var u : userStatsRepo.findAllByOrderByTotalWeightKgDesc(
                PageRequest.of(0, Math.max(1, Math.min(limite, 100))))) {
            entradas.add(new LeaderboardDTO.Entry(
                    puesto++, u.getUserId(), u.getDisplayName(),
                    u.getTotalCollections(), u.getTotalWeightKg(), u.getCo2AvoidedKg()));
        }
        return new LeaderboardDTO(LocalDateTime.now(), entradas);
    }

    @Transactional(readOnly = true)
    public UserStatsDTO usuario(Long userId) {
        return userStatsRepo.findById(userId)
                .map(UserStatsDTO::fromEntity)
                .orElseGet(() -> new UserStatsDTO(userId, null, 0, 0,
                        BigDecimal.ZERO, BigDecimal.ZERO, null, null));
    }

    @Transactional(readOnly = true)
    public List<CenterStatsDTO> centros() {
        return centerStatsRepo.findAllByOrderByTotalWeightKgDesc().stream()
                .map(CenterStatsDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public CenterStatsDTO centro(Long centerId) {
        return centerStatsRepo.findById(centerId)
                .map(CenterStatsDTO::fromEntity)
                .orElseGet(() -> new CenterStatsDTO(centerId, 0,
                        BigDecimal.ZERO, BigDecimal.ZERO, null));
    }

    // ---------------------------------------------------------------

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return parte.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }
}
