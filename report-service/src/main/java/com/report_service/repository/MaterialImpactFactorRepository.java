package com.report_service.repository;

import com.report_service.entity.MaterialImpactFactorEntity;
import com.report_service.enums.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialImpactFactorRepository
        extends JpaRepository<MaterialImpactFactorEntity, MaterialType> {
}
