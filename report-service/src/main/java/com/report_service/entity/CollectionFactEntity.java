package com.report_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Un HECHO: una recoleccion que se completo.
 *
 * Esta tabla es el detalle crudo. Nunca se modifica, solo se inserta.
 * Gracias a ella puedes responder una pregunta que no habias previsto, y
 * sobre todo RECALCULAR los agregados desde cero si te equivocaste en una
 * formula. Sin los hechos, un error en el factor de CO2 seria irreparable.
 */
@Entity
@Table(
        name = "collection_fact",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fact_collection", columnNames = "collectionId"),
        indexes = {
                @Index(name = "idx_fact_requester", columnList = "requesterId"),
                @Index(name = "idx_fact_collector", columnList = "collectorId"),
                @Index(name = "idx_fact_center",    columnList = "recyclingCenterId"),
                @Index(name = "idx_fact_date",      columnList = "completedDate")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionFactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El id de la recoleccion en collection-service.
     *
     * UNIQUE. Esta es la pieza mas importante de todo el servicio: es lo que
     * impide que el mismo evento, reentregado por Kafka, cuente los kilos dos
     * veces. La base de datos te protege aunque tu codigo tenga un bug.
     */
    @Column(nullable = false)
    private Long collectionId;

    @Column(nullable = false)
    private Long requesterId;

    private Long collectorId;
    private Long recyclingCenterId;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    /**
     * El DIA de completedAt, como columna aparte.
     *
     * Parece redundante y no lo es: permite GROUP BY completed_date usando el
     * indice. Con GROUP BY DATE(completed_at), MySQL no puede usar el indice y
     * recorre la tabla entera. Es una tecnica estandar en bases analiticas.
     */
    @Column(nullable = false)
    private LocalDate completedDate;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal totalWeightKg;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal co2AvoidedKg;

    @Column(nullable = false)
    private Integer itemCount;

    /** Cuando lo procese YO (distinto de cuando ocurrio). */
    @Column(nullable = false)
    private LocalDateTime ingestedAt;

    @OneToMany(mappedBy = "collectionFact", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CollectionMaterialFactEntity> materials = new ArrayList<>();

    public void addMaterial(CollectionMaterialFactEntity m) {
        this.materials.add(m);
        m.setCollectionFact(this);
    }
}
