package com.recompensas.repository;

import com.recompensas.entity.MovimientoPuntos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoPuntosRepository extends JpaRepository<MovimientoPuntos, Long> {

    List<MovimientoPuntos> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

}