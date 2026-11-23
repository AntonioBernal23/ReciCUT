package com.collection_service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** El peso REAL que midio el recolector para un material. */
public record ConfirmItemWeightDTO(

        @NotNull(message = "El id del item es obligatorio")
        Long itemId,

        @NotNull(message = "El peso confirmado es obligatorio")
        @DecimalMin(value = "0.000", message = "El peso no puede ser negativo")
        @DecimalMax(value = "9999.999")
        BigDecimal confirmedWeightKg
) {}
