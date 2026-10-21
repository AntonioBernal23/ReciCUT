package com.collection_service.dto;

import com.collection_service.enums.MaterialType;
import com.collection_service.enums.MeasurementUnit;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Un material que el usuario declara al crear la solicitud. */
public record CreateCollectionItemDTO(

        @NotNull(message = "El tipo de material es obligatorio")
        MaterialType materialType,

        @NotNull(message = "La cantidad estimada es obligatoria")
        @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
        @DecimalMax(value = "9999.999", message = "La cantidad es demasiado grande")
        BigDecimal estimatedQuantity,

        @NotNull(message = "La unidad de medida es obligatoria")
        MeasurementUnit unit,

        @Size(max = 255, message = "Las notas no pueden pasar de 255 caracteres")
        String notes
) {}
