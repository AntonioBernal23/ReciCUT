package com.recompensas.repository;

import com.recompensas.entity.SaldoPuntos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaldoPuntosRepository extends JpaRepository<SaldoPuntos, Long> {

    Optional<SaldoPuntos> findByUsuarioId(Long usuarioId);

}