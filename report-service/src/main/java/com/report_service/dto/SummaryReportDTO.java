package com.report_service.dto;

import java.math.BigDecimal;

/** El tablero principal. */
public record SummaryReportDTO(
        PeriodDTO period,
        long totalCollections,
        long cancelledCollections,
        BigDecimal totalWeightKg,
        long activeUsers,
        long activeCollectors,
        BigDecimal averageWeightPerCollectionKg,
        EnvironmentalImpactDTO environmentalImpact
) {}
