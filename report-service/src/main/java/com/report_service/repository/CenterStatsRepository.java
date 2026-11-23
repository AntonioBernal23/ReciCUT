package com.report_service.repository;

import com.report_service.entity.CenterStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CenterStatsRepository extends JpaRepository<CenterStatsEntity, Long> {
    List<CenterStatsEntity> findAllByOrderByTotalWeightKgDesc();
}
