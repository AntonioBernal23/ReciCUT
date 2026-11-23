package com.report_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record LeaderboardDTO(
        LocalDateTime generatedAt,
        List<Entry> entries
) {
    public record Entry(
            int rank,
            Long userId,
            String displayName,
            int totalCollections,
            BigDecimal totalWeightKg,
            BigDecimal co2AvoidedKg
    ) {}
}
