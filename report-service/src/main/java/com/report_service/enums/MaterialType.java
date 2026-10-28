package com.report_service.enums;

/**
 * Copia del enum de collection-service.
 *
 * Si, es codigo "duplicado", y es INTENCIONAL. Los microservicios no
 * comparten codigo, comparten un CONTRATO. Lo que viaja por Kafka es JSON
 * con el texto "PET"; cada servicio lo convierte a SU propio enum.
 *
 * Si compartieran un .jar comun, cambiar este enum obligaria a recompilar y
 * redesplegar los dos servicios al mismo tiempo: justo lo que los
 * microservicios intentan evitar.
 */
public enum MaterialType {
    PET, HDPE, PAPER, CARDBOARD, ALUMINUM, GLASS, TETRA_PAK,
    ELECTRONIC, BATTERY, BOTTLE_CAP, ORGANIC, OTHER
}
