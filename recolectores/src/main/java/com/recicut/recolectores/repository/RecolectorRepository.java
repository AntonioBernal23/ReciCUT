package com.recicut.recolectores.repository;

import com.recicut.recolectores.entity.Recolector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecolectorRepository extends JpaRepository<Recolector, Long> {
}