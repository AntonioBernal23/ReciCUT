package com.report_service.repository;

import com.report_service.entity.CollectionMaterialFactEntity;
import com.report_service.projection.MaterialAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CollectionMaterialFactRepository
        extends JpaRepository<CollectionMaterialFactEntity, Long> {

    /**
     * El desglose por material.
     *
     * El GROUP BY lo hace MySQL, no Java. Traer 50,000 renglones a memoria
     * para sumarlos en un for seria lentisimo: la base esta optimizada
     * justo para esto.
     */
    @Query("""
           SELECT m.materialType                 AS materialType,
                  SUM(m.weightKg)                AS totalWeight,
                  SUM(m.co2AvoidedKg)            AS totalCo2,
                  COUNT(DISTINCT m.collectionId) AS collectionCount
           FROM CollectionMaterialFactEntity m
           WHERE m.completedDate BETWEEN :desde AND :hasta
           GROUP BY m.materialType
           ORDER BY SUM(m.weightKg) DESC
           """)
    List<MaterialAggregate> agregarPorMaterial(@Param("desde") LocalDate desde,
                                               @Param("hasta") LocalDate hasta);
}
