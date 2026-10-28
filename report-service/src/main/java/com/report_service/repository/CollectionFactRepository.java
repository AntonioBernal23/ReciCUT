package com.report_service.repository;

import com.report_service.entity.CollectionFactEntity;
import com.report_service.projection.TimelinePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CollectionFactRepository
        extends JpaRepository<CollectionFactEntity, Long> {

    /** La defensa #2 de la idempotencia: preguntar antes de insertar. */
    boolean existsByCollectionId(Long collectionId);

    long countByCompletedDateBetween(LocalDate desde, LocalDate hasta);

    @Query("""
           SELECT COALESCE(SUM(f.totalWeightKg), 0) FROM CollectionFactEntity f
           WHERE f.completedDate BETWEEN :desde AND :hasta
           """)
    BigDecimal sumPeso(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("""
           SELECT COALESCE(SUM(f.co2AvoidedKg), 0) FROM CollectionFactEntity f
           WHERE f.completedDate BETWEEN :desde AND :hasta
           """)
    BigDecimal sumCo2(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("""
           SELECT COUNT(DISTINCT f.requesterId) FROM CollectionFactEntity f
           WHERE f.completedDate BETWEEN :desde AND :hasta
           """)
    long contarUsuariosActivos(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("""
           SELECT COUNT(DISTINCT f.collectorId) FROM CollectionFactEntity f
           WHERE f.completedDate BETWEEN :desde AND :hasta AND f.collectorId IS NOT NULL
           """)
    long contarRecolectoresActivos(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    /** Serie temporal por dia. Los alias deben coincidir con los getters. */
    @Query("""
           SELECT f.completedDate        AS date,
                  SUM(f.totalWeightKg)   AS weightKg,
                  COUNT(f)               AS collectionCount
           FROM CollectionFactEntity f
           WHERE f.completedDate BETWEEN :desde AND :hasta
           GROUP BY f.completedDate
           ORDER BY f.completedDate
           """)
    List<TimelinePoint> serieDiaria(@Param("desde") LocalDate desde,
                                    @Param("hasta") LocalDate hasta);
}
