package com.report_service.service;

import com.report_service.dto.EnvironmentalImpactDTO;
import com.report_service.entity.MaterialImpactFactorEntity;
import com.report_service.enums.MaterialType;
import com.report_service.projection.MaterialAggregate;
import com.report_service.repository.CollectionMaterialFactRepository;
import com.report_service.repository.MaterialImpactFactorRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Traduce kilos de CO2 en cosas que una persona entiende.
 *
 * Las constantes son aproximaciones divulgativas de uso comun. Igual que
 * los factores de impacto: citen una fuente antes de entregar.
 */
@Component
public class EquivalenceCalculator {

    /** Un arbol absorbe del orden de 22 kg de CO2 al anio. */
    private static final BigDecimal CO2_POR_ARBOL_ANIO = new BigDecimal("22");

    /** Un coche promedio emite del orden de 0.2 kg de CO2 por kilometro. */
    private static final BigDecimal CO2_POR_KM_COCHE = new BigDecimal("0.2");

    private final CollectionMaterialFactRepository materialRepo;
    private final MaterialImpactFactorRepository factorRepo;

    public EquivalenceCalculator(CollectionMaterialFactRepository materialRepo,
                                 MaterialImpactFactorRepository factorRepo) {
        this.materialRepo = materialRepo;
        this.factorRepo = factorRepo;
    }

    public EnvironmentalImpactDTO construir(BigDecimal co2, LocalDate desde, LocalDate hasta) {
        BigDecimal agua = BigDecimal.ZERO;
        BigDecimal energia = BigDecimal.ZERO;

        Map<MaterialType, MaterialImpactFactorEntity> factores = new HashMap<>();
        factorRepo.findAll().forEach(f -> factores.put(f.getMaterialType(), f));

        for (MaterialAggregate fila : materialRepo.agregarPorMaterial(desde, hasta)) {
            MaterialImpactFactorEntity f = factores.get(fila.getMaterialType());
            if (f == null || fila.getTotalWeight() == null) continue;

            if (f.getWaterSavedLitersPerKg() != null) {
                agua = agua.add(fila.getTotalWeight().multiply(f.getWaterSavedLitersPerKg()));
            }
            if (f.getEnergySavedKwhPerKg() != null) {
                energia = energia.add(fila.getTotalWeight().multiply(f.getEnergySavedKwhPerKg()));
            }
        }

        long arboles = co2.divide(CO2_POR_ARBOL_ANIO, 0, RoundingMode.DOWN).longValue();
        long km      = co2.divide(CO2_POR_KM_COCHE, 0, RoundingMode.DOWN).longValue();

        return new EnvironmentalImpactDTO(
                co2.setScale(3, RoundingMode.HALF_UP),
                agua.setScale(2, RoundingMode.HALF_UP),
                energia.setScale(3, RoundingMode.HALF_UP),
                new EnvironmentalImpactDTO.Equivalences(arboles, km));
    }
}
