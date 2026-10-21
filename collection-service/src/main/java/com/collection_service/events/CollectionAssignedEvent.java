package com.collection_service.events;

import java.time.LocalDateTime;

/** Se asigno un recolector. Lo consume collector-service (notificaciones). */
public record CollectionAssignedEvent(
        Long collectionId,
        Long requesterId,
        Long collectorId,
        LocalDateTime assignedAt
) {}
