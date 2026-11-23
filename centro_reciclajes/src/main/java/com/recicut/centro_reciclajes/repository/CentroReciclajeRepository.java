package com.recicut.centro_reciclajes.repository;

import com.recicut.centro_reciclajes.entity.CentroReciclaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentroReciclajeRepository extends JpaRepository<CentroReciclaje, Long> {
}