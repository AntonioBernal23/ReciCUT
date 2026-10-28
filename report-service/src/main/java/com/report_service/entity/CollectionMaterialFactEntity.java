package com.report_service.entity;

import com.report_service.enums.MaterialType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un renglon por (recoleccion, material).
 *
 * Fijate que repite collectionId y completedDate, que ya estan en la tabla
 * padre. Es DESNORMALIZACION a proposito: permite agrupar por material y
 * fecha sin hacer JOIN. En una base analitica repetir columnas para evitar
 * JOINs es una tecnica, no un descuido.
 */
@Entity
@Table(
        name = "collection_material_fact",
        indexes = {
                @Index(name = "idx_cmf_material", columnList = "materialType"),
                @Index(name = "idx_cmf_date",     columnList = "completedDate"),
                @Index(name = "idx_cmf_coll",     columnList = "collectionId")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionMaterialFactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_fact_id", nullable = false)
    private CollectionFactEntity collectionFact;

    @Column(nullable = false)
    private Long collectionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialType materialType;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal weightKg;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal co2AvoidedKg;

    @Column(nullable = false)
    private LocalDate completedDate;
}
