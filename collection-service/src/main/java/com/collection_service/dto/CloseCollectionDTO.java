package com.collection_service.dto;

import jakarta.validation.constraints.Size;

/** Motivo al cancelar o rechazar. El motivo es opcional al cancelar. */
public record CloseCollectionDTO(

        @Size(max = 255, message = "El motivo no puede pasar de 255 caracteres")
        String reason
) {}
