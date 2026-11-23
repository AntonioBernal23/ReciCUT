package com.collection_service.repository;

import com.collection_service.entity.CollectionRequestEntity;
import com.collection_service.enums.CollectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de las solicitudes de recoleccion.
 *
 * Es una INTERFAZ y no tiene implementacion: Spring Data genera la clase real
 * en tiempo de arranque, leyendo el nombre de cada metodo para armar el SQL.
 *
 * Al extender JpaRepository<Entidad, TipoDelId> ya vienen gratis:
 *   save, saveAll, findById, findAll, findAll(Pageable), existsById,
 *   deleteById, delete, count, flush...
 */
@Repository
public interface CollectionRequestRepository
        extends JpaRepository<CollectionRequestEntity, Long> {

    // =================================================================
    // CONSULTAS DERIVADAS DEL NOMBRE
    //
    // Spring parte el nombre en palabras clave y escribe el SQL solo.
    // Si te equivocas en el nombre de un campo, FALLA AL ARRANCAR,
    // no en tiempo de ejecucion. Es un error temprano y eso es bueno.
    // =================================================================

    /** Mis solicitudes, las mas nuevas primero. Para GET /api/collections/mine */
    Page<CollectionRequestEntity> findByRequesterIdOrderByCreatedAtDesc(
            Long requesterId, Pageable pageable);

    /** La cola de pendientes, las mas viejas primero (quien pidio antes, primero). */
    Page<CollectionRequestEntity> findByStatusOrderByCreatedAtAsc(
            CollectionStatus status, Pageable pageable);

    /** El trabajo de un recolector en un estado dado. */
    Page<CollectionRequestEntity> findByCollectorIdAndStatus(
            Long collectorId, CollectionStatus status, Pageable pageable);

    /** Cuantas solicitudes tiene un usuario en cierto estado. */
    long countByRequesterIdAndStatus(Long requesterId, CollectionStatus status);

    /**
     * ¿Esta solicitud es de este usuario?
     * Util para validar permisos sin traer la entidad completa.
     */
    boolean existsByIdAndRequesterId(Long id, Long requesterId);

    /** Solicitudes programadas que ya se pasaron de fecha y nadie atendio. */
    List<CollectionRequestEntity> findByStatusAndScheduledAtBefore(
            CollectionStatus status, LocalDateTime limite);

    // =================================================================
    // CONSULTAS ESCRITAS A MANO (JPQL)
    //
    // Cuando el nombre derivado se volveria absurdo o necesitas algo que
    // la derivacion no soporta, escribes la consulta con @Query.
    //
    // OJO: es JPQL, no SQL. Se habla de CLASES y ATRIBUTOS de Java
    // (CollectionRequestEntity, r.createdAt), no de tablas y columnas.
    // Hibernate lo traduce a SQL de MySQL.
    // =================================================================

    /**
     * Trae la solicitud CON sus items en una sola consulta.
     *
     * Resuelve el problema N+1: sin el JOIN FETCH, Hibernate hace 1 consulta
     * para la solicitud y luego 1 mas por cada acceso a getItems() (porque la
     * relacion es LAZY). Con 20 solicitudes serian 21 viajes a la base.
     * Con JOIN FETCH es 1.
     */
    @Query("""
           SELECT r FROM CollectionRequestEntity r
           LEFT JOIN FETCH r.items
           WHERE r.id = :id
           """)
    Optional<CollectionRequestEntity> findByIdWithItems(@Param("id") Long id);

    /**
     * Busqueda con filtros opcionales.
     *
     * El truco del ":param IS NULL OR ..." permite que cada filtro sea
     * opcional: si llega null, esa condicion siempre es verdadera y no
     * filtra nada. Asi un solo metodo cubre todas las combinaciones en vez
     * de escribir findByStatus, findByStatusAndCollectorId,
     * findByStatusAndCollectorIdAndCreatedAtBetween...
     */
    @Query("""
           SELECT r FROM CollectionRequestEntity r
           WHERE (:status      IS NULL OR r.status      = :status)
             AND (:collectorId IS NULL OR r.collectorId = :collectorId)
             AND (:requesterId IS NULL OR r.requesterId = :requesterId)
             AND (:desde       IS NULL OR r.createdAt  >= :desde)
             AND (:hasta       IS NULL OR r.createdAt  <= :hasta)
           """)
    Page<CollectionRequestEntity> buscarConFiltros(
            @Param("status")      CollectionStatus status,
            @Param("collectorId") Long collectorId,
            @Param("requesterId") Long requesterId,
            @Param("desde")       LocalDateTime desde,
            @Param("hasta")       LocalDateTime hasta,
            Pageable pageable);
}
