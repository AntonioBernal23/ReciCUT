package com.collection_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lo que el cliente manda en POST /api/collections
 *
 * Fijate en lo que NO esta aqui: id, status, requesterId, collectorId,
 * totalWeightKg, createdAt... Todo eso lo decide el servidor. Si el cliente
 * pudiera mandarlos, cualquiera crearia una solicitud ya COMPLETED a nombre
 * de otra persona. Por eso el DTO de entrada nunca es la entidad.
 */
public record CreateCollectionRequestDTO(

        @NotBlank(message = "La ubicacion de recoleccion es obligatoria")
        @Size(max = 255)
        String pickupLocation,

        @Size(max = 500)
        String pickupDetails,

        @Size(max = 500)
        String notes,

        @Future(message = "La fecha programada debe ser futura")
        LocalDateTime scheduledAt,

        @Positive(message = "El id del centro de reciclaje debe ser positivo")
        Long recyclingCenterId,

        @NotEmpty(message = "Debes declarar al menos un material")
        @Size(max = 20, message = "Maximo 20 materiales por solicitud")
        @Valid   // <-- sin esto, las validaciones de CADA item se ignoran
        List<CreateCollectionItemDTO> items
) {}
