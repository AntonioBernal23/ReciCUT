package com.report_service.service;

import com.report_service.enums.MaterialType;
import com.report_service.repository.MaterialImpactFactorRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Convierte kilos reciclados en impacto ambiental, usando el catalogo. */
@Component
public class ImpactCalculator {

    private final MaterialImpactFactorRepository factorRepo;

    public ImpactCalculator(MaterialImpactFactorRepository factorRepo) {
        this.factorRepo = factorRepo;
    }

    public BigDecimal co2De(MaterialType tipo, BigDecimal kilos) {
        if (tipo == null || kilos == null) return BigDecimal.ZERO;
        return factorRepo.findById(tipo)
                .map(f -> kilos.multiply(f.getCo2AvoidedKgPerKg()))
                .orElse(BigDecimal.ZERO)          // material sin factor -> 0, no explota
                .setScale(3, RoundingMode.HALF_UP);
    }
}
