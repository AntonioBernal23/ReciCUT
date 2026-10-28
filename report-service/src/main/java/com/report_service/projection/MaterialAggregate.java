package com.report_service.projection;

import com.report_service.enums.MaterialType;

import java.math.BigDecimal;

/**
 * Proyeccion a interfaz.
 *
 * Spring Data la implementa sola: basta con que cada columna del SELECT
 * tenga un alias igual al nombre del getter (sin "get" y en minuscula).
 *
 * Es mucho mejor que List<Object[]>, que es lo que hace la mayoria: ahi
 * accedes a fila[0], fila[1] y haces casts a ciegas. Aqui tienes tipos y
 * nombres, y el compilador te ayuda.
 */
public interface MaterialAggregate {
    MaterialType getMaterialType();
    BigDecimal   getTotalWeight();
    BigDecimal   getTotalCo2();
    Long         getCollectionCount();
}
