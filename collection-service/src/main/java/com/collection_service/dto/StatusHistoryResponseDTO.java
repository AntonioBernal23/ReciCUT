package com.collection_service.dto;

import com.collection_service.entity.CollectionStatusHistoryEntity;
import com.collection_service.enums.CollectionStatus;

import java.time.LocalDateTime;

public record StatusHistoryResponseDTO(
        Long id,
        CollectionStatus fromStatus,
        CollectionStatus toStatus,
        Long changedByUserId,
        String reason,
        LocalDateTime changedAt
) {
    public static StatusHistoryResponseDTO fromEntity(CollectionStatusHistoryEntity e) {
        return new StatusHistoryResponseDTO(
                e.getId(), e.getFromStatus(), e.getToStatus(),
                e.getChangedByUserId(), e.getReason(), e.getChangedAt()
        );
    }
}
