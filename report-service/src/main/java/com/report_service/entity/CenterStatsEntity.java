package com.report_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** AGREGADO por centro de reciclaje. Mismo patron que UserStats. */
@Entity
@Table(name = "center_stats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CenterStatsEntity {

    @Id
    private Long centerId;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalCollections = 0;

    @Column(nullable = false, precision = 14, scale = 3)
    @Builder.Default
    private BigDecimal totalWeightKg = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 3)
    @Builder.Default
    private BigDecimal co2AvoidedKg = BigDecimal.ZERO;

    private LocalDateTime lastCollectionAt;
}
