package com.report_service.dto;

import com.report_service.enums.Granularity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TimelineReportDTO(
        Granularity granularity,
        PeriodDTO period,
        List<Point> points
) {
    public record Point(LocalDate date, BigDecimal weightKg, long collectionCount) {}
}
