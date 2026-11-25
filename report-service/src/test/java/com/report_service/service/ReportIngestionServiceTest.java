package com.report_service.service;

import com.report_service.entity.CollectionFactEntity;
import com.report_service.enums.MaterialType;
import com.report_service.events.CollectionCompletedEvent;
import com.report_service.repository.CenterStatsRepository;
import com.report_service.repository.CollectionFactRepository;
import com.report_service.repository.DailyMaterialSummaryRepository;
import com.report_service.repository.UserStatsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PRUEBA 4 — Idempotencia del consumidor de Kafka.
 *
 * Es LA prueba que hay que saber defender en la exposicion.
 *
 * Kafka garantiza "al menos una entrega", no "exactamente una". Un
 * reinicio del servicio, un rebalanceo del grupo de consumidores o un
 * commit de offset que no alcanzo a guardarse hacen que el MISMO evento
 * llegue dos veces. Es comportamiento normal, no una falla.
 *
 * Sin la defensa de idempotencia, los kilos se contarian doble y nadie se
 * daria cuenta: no hay error, no hay excepcion, solo reportes mal.
 */
@ExtendWith(MockitoExtension.class)
class ReportIngestionServiceTest {

    @Mock CollectionFactRepository factRepo;
    @Mock DailyMaterialSummaryRepository dailyRepo;
    @Mock UserStatsRepository userStatsRepo;
    @Mock CenterStatsRepository centerStatsRepo;
    @Mock ImpactCalculator impacto;

    private ReportIngestionService servicio() {
        return new ReportIngestionService(factRepo, dailyRepo, userStatsRepo,
                centerStatsRepo, impacto);
    }

    private CollectionCompletedEvent evento() {
        return new CollectionCompletedEvent(
                42L, 1L, 3L, 2L,
                LocalDateTime.of(2026, 11, 23, 10, 30),
                new BigDecimal("4.500"),
                List.of(new CollectionCompletedEvent.Item(
                        MaterialType.PET, new BigDecimal("4.500"))));
    }

    @Test
    @DisplayName("Un evento nuevo se guarda")
    void eventoNuevoSeGuarda() {
        when(factRepo.existsByCollectionId(42L)).thenReturn(false);
        when(impacto.co2De(any(), any())).thenReturn(new BigDecimal("9.000"));

        servicio().ingerirCompletada(evento());

        verify(factRepo).save(any(CollectionFactEntity.class));
    }

    /**
     * El corazon de la prueba: el mismo evento, dos veces, UNA sola fila.
     *
     * La defensa es `existsByCollectionId`: el id de la recoleccion es la
     * llave de idempotencia. Viene del evento, no se genera aqui, asi que
     * dos entregas del mismo evento traen el mismo id.
     */
    @Test
    @DisplayName("El MISMO evento dos veces se guarda UNA sola vez")
    void eventoDuplicadoSeIgnora() {
        CollectionCompletedEvent e = evento();
        ReportIngestionService servicio = servicio();

        // Primera entrega: no existe todavia -> se guarda
        when(factRepo.existsByCollectionId(42L)).thenReturn(false);
        when(impacto.co2De(any(), any())).thenReturn(new BigDecimal("9.000"));
        servicio.ingerirCompletada(e);

        // Segunda entrega del MISMO evento: ya existe -> se ignora
        when(factRepo.existsByCollectionId(42L)).thenReturn(true);
        servicio.ingerirCompletada(e);

        // Una sola fila, aunque el evento llego dos veces
        verify(factRepo, times(1)).save(any(CollectionFactEntity.class));
    }

    /**
     * Y el duplicado no debe tocar NINGUN agregado. Si solo se protegiera
     * la tabla de hechos pero los totales diarios se sumaran igual, el
     * detalle diria 4.5 kg y el resumen 9 kg: peor que no tener la
     * defensa, porque ahora dos pantallas se contradicen.
     */
    @Test
    @DisplayName("Un duplicado no suma a los agregados")
    void duplicadoNoTocaAgregados() {
        when(factRepo.existsByCollectionId(42L)).thenReturn(true);

        servicio().ingerirCompletada(evento());

        verify(factRepo, never()).save(any());
        verify(dailyRepo, never()).save(any());
        verify(userStatsRepo, never()).save(any());
        verify(centerStatsRepo, never()).save(any());
    }
}
