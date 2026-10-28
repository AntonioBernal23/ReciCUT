package com.collection_service.events;

import java.time.LocalDateTime;

/** Se creo una solicitud. Lo consumen collector-service y report-service. */
public record CollectionRequestedEvent(
        Long collectionId,
        Long requesterId,
        Long recyclingCenterId,
        String pickupLocation,
        LocalDateTime scheduledAt,
        LocalDateTime createdAt
) {}
