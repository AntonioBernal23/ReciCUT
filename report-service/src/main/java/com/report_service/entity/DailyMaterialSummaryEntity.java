package com.report_service.entity;

import com.report_service.enums.MaterialType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AGREGADO: totales por (dia, material), ya sumados.
 *
 * Cuando llega un evento se SUMA al renglon que corresponde. Asi la grafica
 * de "kilos por material del ultimo mes" es un SELECT de ~30 renglones en
 * vez de un SUM() sobre miles de hechos.
 *
 * La llave unica (date, materialType) es lo que permite el UPSERT atomico
 * del repositorio: insertar si no existe, sumar si ya existe.
 */
@Entity
@Table(
        name = "daily_material_summary",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dms_date_material", columnNames = {"summaryDate", "materialType"}),
        indexes = @Index(name = "idx_dms_date", columnList = "summaryDate")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyMaterialSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** "date" es palabra reservada en SQL; por eso summaryDate. */
    @Column(nullable = false)
    private LocalDate summaryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialType materialType;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal totalWeightKg;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal co2AvoidedKg;

    @Column(nullable = false)
    private Integer collectionCount;
}
