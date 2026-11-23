package com.report_service.events;

import com.report_service.enums.MaterialType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Copia del contrato que publica collection-service.
 *
 * Los campos deben COINCIDIR con el JSON que llega. Si el otro equipo
 * renombra un campo, aqui llega null sin avisar. Por eso un evento es un
 * contrato publico y no se cambia a la ligera.
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
    public record Item(MaterialType materialType, BigDecimal weightKg) {}
}
