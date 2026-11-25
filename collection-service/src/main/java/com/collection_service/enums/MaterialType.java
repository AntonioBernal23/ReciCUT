package com.collection_service.enums;

/**
 * Tipos de material reciclable que se pueden solicitar para recoleccion.
 *
 * Es un enum y no una tabla de catalogo por simplicidad: agregar un material
 * nuevo implica recompilar, pero a cambio el compilador valida cada uso y no
 * hay que mantener datos semilla en la base.
 */
public enum MaterialType {

    PET,          // botellas de refresco y agua
    HDPE,         // plastico rigido: garrafones, envases de detergente
    PAPER,        // hojas, cuadernos, periodico
    CARDBOARD,    // carton y cajas
    ALUMINUM,     // latas
    GLASS,        // vidrio
    TETRA_PAK,    // envases de jugo y leche
    ELECTRONIC,   // cables, cargadores, dispositivos pequenios
    BATTERY,      // pilas
    BOTTLE_CAP,   // tapitas (campania clasica en escuelas)
    ORGANIC,      // residuos organicos para composta
    OTHER
}
