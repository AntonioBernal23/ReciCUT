package com.report_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AGREGADO por usuario. Alimenta el ranking.
 *
 * displayName es una COPIA del nombre que "pertenece" a user-service.
 * Se llena escuchando el evento user-registered. La alternativa seria
 * llamar a user-service 10 veces para pintar un top-10, y si esta caido,
 * no hay ranking. Duplicar el dato a proposito se llama desnormalizacion
 * y en microservicios es la solucion, no el problema.
 */
@Entity
@Table(
        name = "user_stats",
        indexes = @Index(name = "idx_us_weight", columnList = "totalWeightKg")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserStatsEntity {

    /** El userId de auth-service ES la llave. No hay FK: otra base de datos. */
    @Id
    private Long userId;

    @Column(length = 150)
    private String displayName;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalCollections = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer cancelledCollections = 0;

    @Column(nullable = false, precision = 14, scale = 3)
    @Builder.Default
    private BigDecimal totalWeightKg = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 3)
    @Builder.Default
    private BigDecimal co2AvoidedKg = BigDecimal.ZERO;

    private LocalDateTime firstCollectionAt;
    private LocalDateTime lastCollectionAt;
}
