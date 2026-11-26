package com.recompensas.repository;

import com.recompensas.entity.Canje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanjeRepository extends JpaRepository<Canje, Long> {

    List<Canje> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

}