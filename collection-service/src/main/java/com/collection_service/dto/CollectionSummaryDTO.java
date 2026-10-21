package com.collection_service.dto;

import com.collection_service.entity.CollectionRequestEntity;
import com.collection_service.enums.CollectionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Version ligera para listados.
 *
 * No trae los items: en una lista de 20 solicitudes no quieres 60 materiales
 * en el JSON, y sobre todo no quieres que Hibernate los cargue (problema N+1).
 */
public record CollectionSummaryDTO(
        Long id,
        Long requesterId,
        Long collectorId,
        CollectionStatus status,
        String pickupLocation,
        LocalDateTime scheduledAt,
        LocalDateTime completedAt,
        BigDecimal totalWeightKg,
        LocalDateTime createdAt
) {
    public static CollectionSummaryDTO fromEntity(CollectionRequestEntity e) {
        return new CollectionSummaryDTO(
                e.getId(), e.getRequesterId(), e.getCollectorId(), e.getStatus(),
                e.getPickupLocation(), e.getScheduledAt(), e.getCompletedAt(),
                e.getTotalWeightKg(), e.getCreatedAt()
        );
    }
}
