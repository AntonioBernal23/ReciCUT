package com.report_service.events;

import java.time.LocalDateTime;

public record CollectionCancelledEvent(
        Long collectionId,
        Long requesterId,
        String finalStatus,
        String reason,
        LocalDateTime closedAt
) {}
