package com.collection_service.entity;

import com.collection_service.enums.MaterialType;
import com.collection_service.enums.MeasurementUnit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Un material dentro de una solicitud.
 *
 * Es tabla aparte y no columnas en CollectionRequestEntity porque una
 * solicitud puede llevar varios materiales: 20 botellas de PET Y 3 kg de
 * carton. Si fueran columnas, tendrias material1, material2, material3...
 * y nunca alcanzarian.
 */
@Entity
@Table(
        name = "collection_item",
        indexes = @Index(name = "idx_ci_request", columnList = "request_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * La solicitud a la que pertenece.
     *
     * Este SI es @ManyToOne de verdad (con llave foranea) porque las dos
     * tablas estan en la misma base de datos. Compara con requesterId en
     * CollectionRequestEntity, que es un Long pelado por estar en otra base.
     *
     * LAZY: al cargar un item no traigas la solicitud completa a menos que
     * alguien la pida. El default de @ManyToOne es EAGER y suele sobrar.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private CollectionRequestEntity request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialType materialType;

    // ---------------------------------------------------------------
    // Los DOS datos de cantidad. No son redundantes:
    //
    //   estimatedQuantity + unit  -> lo que DECLARA el usuario ("20 piezas")
    //                                sirve para planear la ruta
    //   confirmedWeightKg         -> lo que MIDE el recolector (1.240 kg)
    //                                es el unico valido para reportes
    //
    // Si guardaras una sola "cantidad", report-service estaria sumando
    // "20 piezas + 3 kilos + 2 bolsas" y el total no significaria nada.
    // ---------------------------------------------------------------

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal estimatedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MeasurementUnit unit;

    /** Nulo hasta que la recoleccion se completa. */
    @Column(precision = 8, scale = 3)
    private BigDecimal confirmedWeightKg;

    @Column(length = 255)
    private String notes;
}
