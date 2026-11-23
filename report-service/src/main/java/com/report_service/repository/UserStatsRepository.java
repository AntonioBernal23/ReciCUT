package com.report_service.repository;

import com.report_service.entity.UserStatsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStatsEntity, Long> {

    /** El ranking. Pageable da el LIMIT sin escribirlo a mano. */
    List<UserStatsEntity> findAllByOrderByTotalWeightKgDesc(Pageable pageable);
}
