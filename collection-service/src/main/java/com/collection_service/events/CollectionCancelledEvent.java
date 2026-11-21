package com.collection_service.events;

import com.collection_service.enums.CollectionStatus;

import java.time.LocalDateTime;

/** Se cancelo o rechazo. report-service lo usa para la tasa de cancelacion. */
public record CollectionCancelledEvent(
        Long collectionId,
        Long requesterId,
        CollectionStatus finalStatus,
        String reason,
        LocalDateTime closedAt
) {}
