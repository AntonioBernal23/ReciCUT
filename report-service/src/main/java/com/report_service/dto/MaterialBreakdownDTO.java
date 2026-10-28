package com.report_service.dto;

import com.report_service.enums.MaterialType;

import java.math.BigDecimal;
import java.util.List;

public record MaterialBreakdownDTO(
        PeriodDTO period,
        BigDecimal totalWeightKg,
        List<MaterialLine> materials
) {
    public record MaterialLine(
            MaterialType materialType,
            BigDecimal weightKg,
            BigDecimal percentage,
            long collectionCount,
            BigDecimal co2AvoidedKg
    ) {}
}
