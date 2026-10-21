package com.collection_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Completar una recoleccion: el recolector captura el peso real de cada
 * material. De aqui sale el evento collection-completed que alimenta a
 * report-service.
 */
public record CompleteCollectionDTO(

        @NotEmpty(message = "Debes confirmar el peso de al menos un material")
        @Valid
        List<ConfirmItemWeightDTO> items,

        @Size(max = 500) String notes
) {}
