package com.collection_service.repository;

import com.collection_service.entity.CollectionStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Bitacora de cambios de estado.
 *
 * Solo se escribe (un renglon por transicion) y se lee en orden cronologico
 * para mostrar la linea de tiempo de una solicitud. No necesita mas.
 */
@Repository
public interface CollectionStatusHistoryRepository
        extends JpaRepository<CollectionStatusHistoryEntity, Long> {

    /**
     * La linea de tiempo de una solicitud, del cambio mas viejo al mas nuevo.
     *
     * Fijate en "RequestId": Spring navega la relacion @ManyToOne 'request'
     * y usa su campo 'id'. No hace falta un atributo requestId en la entidad.
     */
    List<CollectionStatusHistoryEntity> findByRequestIdOrderByChangedAtAsc(Long requestId);

    long countByRequestId(Long requestId);
}
