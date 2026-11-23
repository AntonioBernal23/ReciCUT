package com.collection_service.dto;

import com.collection_service.entity.CollectionItemEntity;
import com.collection_service.enums.MaterialType;
import com.collection_service.enums.MeasurementUnit;

import java.math.BigDecimal;

public record CollectionItemResponseDTO(
        Long id,
        MaterialType materialType,
        BigDecimal estimatedQuantity,
        MeasurementUnit unit,
        BigDecimal confirmedWeightKg,
        String notes
) {
    public static CollectionItemResponseDTO fromEntity(CollectionItemEntity e) {
        return new CollectionItemResponseDTO(
                e.getId(),
                e.getMaterialType(),
                e.getEstimatedQuantity(),
                e.getUnit(),
                e.getConfirmedWeightKg(),
                e.getNotes()
        );
    }
}
