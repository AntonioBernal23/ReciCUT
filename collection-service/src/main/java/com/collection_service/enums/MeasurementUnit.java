package com.collection_service.enums;

/**
 * Unidad en la que el usuario DECLARA cuanto material tiene.
 *
 * Es solo un estimado para que el recolector planee su ruta: nadie espera que
 * un estudiante pese su carton antes de pedir la recoleccion.
 *
 * El dato real y confiable es el peso en kilogramos que captura el recolector
 * al completar (CollectionItemEntity.confirmedWeightKg), y ese es el unico
 * que se usa para reportes e impacto ambiental.
 */
public enum MeasurementUnit {

    KILOGRAM,   // ya viene pesado
    PIECE,      // "20 botellas"
    BAG,        // "2 bolsas grandes"
    LITER       // para volumenes
}
