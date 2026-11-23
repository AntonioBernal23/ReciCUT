package com.report_service.repository;

import com.report_service.entity.DailyMaterialSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyMaterialSummaryRepository
        extends JpaRepository<DailyMaterialSummaryEntity, Long> {

    /**
     * UPSERT: inserta si no existe, suma si ya existe. En UNA sola sentencia.
     *
     * Por que no "buscar, si no existe insertar, si existe sumar": entre el
     * SELECT y el INSERT otro hilo puede insertar el mismo (dia, material) y
     * truena la restriccion unica. Es una condicion de carrera clasica.
     * ON DUPLICATE KEY UPDATE lo resuelve de forma atomica en la base.
     *
     * nativeQuery = true porque ON DUPLICATE KEY UPDATE es sintaxis de MySQL,
     * no de JPQL.
     */
    @Modifying
    @Query(value = """
           INSERT INTO daily_material_summary
                  (summary_date, material_type, total_weight_kg, co2_avoided_kg, collection_count)
           VALUES (:fecha, :material, :peso, :co2, 1)
           ON DUPLICATE KEY UPDATE
                  total_weight_kg  = total_weight_kg  + :peso,
                  co2_avoided_kg   = co2_avoided_kg   + :co2,
                  collection_count = collection_count + 1
           """, nativeQuery = true)
    void acumular(@Param("fecha") LocalDate fecha,
                  @Param("material") String material,
                  @Param("peso") BigDecimal peso,
                  @Param("co2") BigDecimal co2);

    List<DailyMaterialSummaryEntity> findBySummaryDateBetweenOrderBySummaryDateAsc(
            LocalDate desde, LocalDate hasta);
}
