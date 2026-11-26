package com.recompensas.repository;

import com.recompensas.entity.MovimientoPuntos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoPuntosRepository extends JpaRepository<MovimientoPuntos, Long> {

    List<MovimientoPuntos> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    boolean existsByUsuarioIdAndReferencia(Long usuarioId, String referencia);
}