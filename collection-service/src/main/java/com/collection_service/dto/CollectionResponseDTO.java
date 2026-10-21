package com.collection_service.dto;

import com.collection_service.entity.CollectionRequestEntity;
import com.collection_service.enums.CollectionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * La vista completa de una solicitud.
 *
 * 'nextPossibleStatuses' no existe en la base: se calcula desde la maquina
 * de estados. Le dice al frontend que botones pintar sin tener que duplicar
 * las reglas de negocio en JavaScript.
 */
public record CollectionResponseDTO(
        Long id,
        Long requesterId,
        Long collectorId,
        Long recyclingCenterId,
        CollectionStatus status,
        Set<CollectionStatus> nextPossibleStatuses,
        String pickupLocation,
        String pickupDetails,
        String notes,
        LocalDateTime scheduledAt,
        LocalDateTime assignedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime closedAt,
        String closeReason,
        BigDecimal totalWeightKg,
        List<CollectionItemResponseDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CollectionResponseDTO fromEntity(CollectionRequestEntity e) {
        return new CollectionResponseDTO(
                e.getId(),
                e.getRequesterId(),
                e.getCollectorId(),
                e.getRecyclingCenterId(),
                e.getStatus(),
                e.getStatus().siguientesPosibles(),
                e.getPickupLocation(),
                e.getPickupDetails(),
                e.getNotes(),
                e.getScheduledAt(),
                e.getAssignedAt(),
                e.getStartedAt(),
                e.getCompletedAt(),
                e.getClosedAt(),
                e.getCloseReason(),
                e.getTotalWeightKg(),
                e.getItems().stream().map(CollectionItemResponseDTO::fromEntity).toList(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
