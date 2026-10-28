package com.report_service.dto;

import com.report_service.entity.CenterStatsEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CenterStatsDTO(
        Long centerId,
        int totalCollections,
        BigDecimal totalWeightKg,
        BigDecimal co2AvoidedKg,
        LocalDateTime lastCollectionAt
) {
    public static CenterStatsDTO fromEntity(CenterStatsEntity e) {
        return new CenterStatsDTO(e.getCenterId(), e.getTotalCollections(),
                e.getTotalWeightKg(), e.getCo2AvoidedKg(), e.getLastCollectionAt());
    }
}
