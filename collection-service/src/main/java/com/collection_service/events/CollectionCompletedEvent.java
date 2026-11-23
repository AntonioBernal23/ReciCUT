package com.collection_service.events;

import com.collection_service.enums.MaterialType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EL evento importante del sistema.
 *
 * Lo consumen report-service (estadisticas e impacto ambiental),
 * reward-service (puntos) y campaign-service (avance de retos).
 *
 * Es AUTOCONTENIDO: trae todo lo que el consumidor necesita. Si solo
 * mandara el collectionId, tres servicios tendrian que llamarnos por HTTP
 * por cada recoleccion, y si collection-service esta caido se bloquean.
 *
 * Una vez que otro equipo lo consuma, cambiarle un campo le rompe el codigo:
 * esto es un CONTRATO publico, no una clase interna.
 */
public record CollectionCompletedEvent(
        Long collectionId,
        Long requesterId,
        Long collectorId,
        Long recyclingCenterId,
        LocalDateTime completedAt,
        BigDecimal totalWeightKg,
        List<Item> items
) {
    /** Solo el peso REAL medido. La cantidad estimada no sirve para reportes. */
    public record Item(MaterialType materialType, BigDecimal weightKg) {}
}
