package com.report_service.entity;

import com.report_service.enums.MaterialType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Catalogo: cuanto impacto ambiental se evita por cada kilo reciclado.
 *
 * Es una TABLA y no constantes en el codigo por dos razones:
 *   1. Puedes ajustar un factor sin recompilar.
 *   2. Puedes RECALCULAR todo el historico con los valores nuevos,
 *      porque los hechos crudos siguen guardados.
 *
 * IMPORTANTE: los valores que trae el seeder son aproximaciones de
 * referencia. Busquen fuentes reales (SEMARNAT, EPA, articulos academicos)
 * y llenen sourceReference. Un profesor va a preguntar de donde salieron.
 */
@Entity
@Table(name = "material_impact_factor")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaterialImpactFactorEntity {

    /** El propio enum es la llave primaria: no hace falta un id autoincremental. */
    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MaterialType materialType;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal co2AvoidedKgPerKg;

    @Column(precision = 12, scale = 2)
    private BigDecimal waterSavedLitersPerKg;

    @Column(precision = 10, scale = 4)
    private BigDecimal energySavedKwhPerKg;

    @Column(length = 255)
    private String description;

    /** De donde salio el numero. Sin esto, el dato no vale nada. */
    @Column(length = 255)
    private String sourceReference;
}
