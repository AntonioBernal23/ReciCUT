package com.collection_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** Editar una solicitud. Solo se permite mientras siga en PENDING. */
public record UpdateCollectionRequestDTO(

        @NotBlank @Size(max = 255) String pickupLocation,
        @Size(max = 500) String pickupDetails,
        @Size(max = 500) String notes,
        @Future LocalDateTime scheduledAt,
        @Positive Long recyclingCenterId,

        @NotEmpty @Size(max = 20) @Valid
        List<CreateCollectionItemDTO> items
) {}
