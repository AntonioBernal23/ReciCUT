package com.campania_service.repository;

import com.campania_service.entity.InscripcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<InscripcionEntity, Long> {

    boolean existsByCampaniaIdAndUsuarioId(Long campaniaId, Long usuarioId);

    Optional<InscripcionEntity> findByCampaniaIdAndUsuarioId(Long campaniaId, Long usuarioId);

    List<InscripcionEntity> findByUsuarioId(Long usuarioId);
}