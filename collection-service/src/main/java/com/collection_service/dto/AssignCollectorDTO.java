package com.collection_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Asignar un recolector a una solicitud PENDING. */
public record AssignCollectorDTO(

        @NotNull(message = "El id del recolector es obligatorio")
        @Positive
        Long collectorId
) {}
