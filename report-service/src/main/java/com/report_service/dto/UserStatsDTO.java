package com.report_service.dto;

import com.report_service.entity.UserStatsEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserStatsDTO(
        Long userId,
        String displayName,
        int totalCollections,
        int cancelledCollections,
        BigDecimal totalWeightKg,
        BigDecimal co2AvoidedKg,
        LocalDateTime firstCollectionAt,
        LocalDateTime lastCollectionAt
) {
    public static UserStatsDTO fromEntity(UserStatsEntity e) {
        return new UserStatsDTO(e.getUserId(), e.getDisplayName(),
                e.getTotalCollections(), e.getCancelledCollections(),
                e.getTotalWeightKg(), e.getCo2AvoidedKg(),
                e.getFirstCollectionAt(), e.getLastCollectionAt());
    }
}
