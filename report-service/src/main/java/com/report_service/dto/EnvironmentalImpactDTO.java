package com.report_service.dto;

import java.math.BigDecimal;

/**
 * El impacto ambiental, con EQUIVALENCIAS.
 *
 * "1247 kg de CO2" no le dice nada a nadie. "Equivale a 57 arboles" si.
 * Es una division por una constante y cambia por completo como se percibe
 * tu servicio. Para un proyecto de reciclaje del campus, es justo el dato
 * que la gente entiende.
 */
public record EnvironmentalImpactDTO(
        BigDecimal co2AvoidedKg,
        BigDecimal waterSavedLiters,
        BigDecimal energySavedKwh,
        Equivalences equivalences
) {
    public record Equivalences(long treesEquivalent, long kmNotDrivenByCar) {}
}
