package com.collection_service.service;

import com.collection_service.dto.*;
import com.collection_service.entity.CollectionItemEntity;
import com.collection_service.entity.CollectionRequestEntity;
import com.collection_service.entity.CollectionStatusHistoryEntity;
import com.collection_service.enums.CollectionStatus;
import com.collection_service.events.CollectionEventProducer;
import com.collection_service.exception.CollectionNotFoundException;
import com.collection_service.exception.ForbiddenOperationException;
import com.collection_service.exception.InvalidStatusTransitionException;
import com.collection_service.repository.CollectionRequestRepository;
import com.collection_service.repository.CollectionStatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * La logica de negocio de las recolecciones.
 *
 * Esta capa NO sabe nada de HTTP: no devuelve codigos de estado ni toca
 * ResponseEntity. Cuando algo esta mal, lanza una excepcion y el
 * GlobalExceptionHandler decide que codigo HTTP le corresponde.
 */
@Service
public class CollectionService {

    private static final String ROL_ADMIN     = "ADMIN";
    private static final String ROL_COLLECTOR = "COLLECTOR";

    private final CollectionRequestRepository requestRepository;
    private final CollectionStatusHistoryRepository historyRepository;
    private final CollectionEventProducer eventProducer;

    public CollectionService(CollectionRequestRepository requestRepository,
                             CollectionStatusHistoryRepository historyRepository,
                             CollectionEventProducer eventProducer) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.eventProducer = eventProducer;
    }

    // =================================================================
    // ESCRITURA
    // =================================================================

    @Transactional
    public CollectionResponseDTO crear(Long userId, CreateCollectionRequestDTO dto) {

        CollectionRequestEntity solicitud = CollectionRequestEntity.builder()
                .requesterId(userId)                      // del header, NUNCA del body
                .status(CollectionStatus.PENDING)
                .pickupLocation(dto.pickupLocation())
                .pickupDetails(dto.pickupDetails())
                .notes(dto.notes())
                .scheduledAt(dto.scheduledAt())
                .recyclingCenterId(dto.recyclingCenterId())
                .build();

        // addItem mantiene los DOS lados de la relacion bidireccional
        dto.items().forEach(i -> solicitud.addItem(CollectionItemEntity.builder()
                .materialType(i.materialType())
                .estimatedQuantity(i.estimatedQuantity())
                .unit(i.unit())
                .notes(i.notes())
                .build()));

        CollectionRequestEntity guardada = requestRepository.save(solicitud);

        registrarHistorial(guardada, null, CollectionStatus.PENDING, userId,
                "Solicitud creada");
        eventProducer.publicarCreada(guardada);

        return CollectionResponseDTO.fromEntity(guardada);
    }

    @Transactional
    public CollectionResponseDTO actualizar(Long id, Long userId,
                                            UpdateCollectionRequestDTO dto) {
        CollectionRequestEntity s = buscarOFallar(id);

        if (!s.getRequesterId().equals(userId)) {
            throw new ForbiddenOperationException("Esta solicitud no es tuya");
        }
        if (s.getStatus() != CollectionStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Solo puedes editar una solicitud en PENDING; esta esta en "
                            + s.getStatus());
        }

        s.setPickupLocation(dto.pickupLocation());
        s.setPickupDetails(dto.pickupDetails());
        s.setNotes(dto.notes());
        s.setScheduledAt(dto.scheduledAt());
        s.setRecyclingCenterId(dto.recyclingCenterId());

        // orphanRemoval = true -> limpiar la lista borra los items viejos de la BD
        s.getItems().clear();
        dto.items().forEach(i -> s.addItem(CollectionItemEntity.builder()
                .materialType(i.materialType())
                .estimatedQuantity(i.estimatedQuantity())
                .unit(i.unit())
                .notes(i.notes())
                .build()));

        return CollectionResponseDTO.fromEntity(requestRepository.save(s));
    }

    @Transactional
    public CollectionResponseDTO asignar(Long id, Long userId, String rol,
                                         AssignCollectorDTO dto) {
        CollectionRequestEntity s = buscarOFallar(id);
        exigirRol(rol, "Solo un administrador o un recolector puede asignar",
                ROL_ADMIN, ROL_COLLECTOR);
        exigirTransicion(s, CollectionStatus.ASSIGNED);

        s.setCollectorId(dto.collectorId());
        s.setAssignedAt(LocalDateTime.now());
        cambiarEstado(s, CollectionStatus.ASSIGNED, userId, null);

        CollectionRequestEntity guardada = requestRepository.save(s);
        eventProducer.publicarAsignada(guardada);
        return CollectionResponseDTO.fromEntity(guardada);
    }

    @Transactional
    public CollectionResponseDTO iniciar(Long id, Long userId, String rol) {
        CollectionRequestEntity s = buscarOFallar(id);
        exigirTransicion(s, CollectionStatus.IN_PROGRESS);
        exigirRecolectorAsignadoOAdmin(s, userId, rol);

        s.setStartedAt(LocalDateTime.now());
        cambiarEstado(s, CollectionStatus.IN_PROGRESS, userId, null);

        return CollectionResponseDTO.fromEntity(requestRepository.save(s));
    }

    /** El metodo mas importante: aqui nace el evento que alimenta a reportes. */
    @Transactional
    public CollectionResponseDTO completar(Long id, Long userId, String rol,
                                           CompleteCollectionDTO dto) {
        CollectionRequestEntity s = buscarOFallar(id);
        exigirTransicion(s, CollectionStatus.COMPLETED);
        exigirRecolectorAsignadoOAdmin(s, userId, rol);

        Map<Long, CollectionItemEntity> porId = s.getItems().stream()
                .collect(Collectors.toMap(CollectionItemEntity::getId, Function.identity()));

        for (ConfirmItemWeightDTO confirmado : dto.items()) {
            CollectionItemEntity item = porId.get(confirmado.itemId());
            if (item == null) {
                throw new InvalidStatusTransitionException(
                        "El material %d no pertenece a la solicitud %d"
                                .formatted(confirmado.itemId(), id));
            }
            item.setConfirmedWeightKg(confirmado.confirmedWeightKg());
        }

        // El total se calcula AQUI, nunca se acepta del cliente
        BigDecimal total = s.getItems().stream()
                .map(CollectionItemEntity::getConfirmedWeightKg)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        s.setTotalWeightKg(total);
        s.setCompletedAt(LocalDateTime.now());
        if (dto.notes() != null) {
            s.setNotes(dto.notes());
        }
        cambiarEstado(s, CollectionStatus.COMPLETED, userId, null);

        CollectionRequestEntity guardada = requestRepository.save(s);
        eventProducer.publicarCompletada(guardada);
        return CollectionResponseDTO.fromEntity(guardada);
    }

    @Transactional
    public CollectionResponseDTO cancelar(Long id, Long userId, String rol,
                                          CloseCollectionDTO dto) {
        CollectionRequestEntity s = buscarOFallar(id);
        exigirTransicion(s, CollectionStatus.CANCELLED);

        boolean esDueno = s.getRequesterId().equals(userId);
        boolean esAdmin = ROL_ADMIN.equals(rol);

        if (!esDueno && !esAdmin) {
            throw new ForbiddenOperationException("Esta solicitud no es tuya");
        }
        // Si el recolector ya va en camino, solo un admin puede cancelar
        if (s.getStatus() == CollectionStatus.IN_PROGRESS && !esAdmin) {
            throw new ForbiddenOperationException(
                    "Una recoleccion en curso solo la puede cancelar un administrador");
        }

        s.setClosedAt(LocalDateTime.now());
        s.setCloseReason(dto == null ? null : dto.reason());
        cambiarEstado(s, CollectionStatus.CANCELLED, userId,
                dto == null ? null : dto.reason());

        CollectionRequestEntity guardada = requestRepository.save(s);
        eventProducer.publicarCerrada(guardada);
        return CollectionResponseDTO.fromEntity(guardada);
    }

    @Transactional
    public CollectionResponseDTO rechazar(Long id, Long userId, String rol,
                                          CloseCollectionDTO dto) {
        CollectionRequestEntity s = buscarOFallar(id);
        exigirRol(rol, "Solo un administrador puede rechazar", ROL_ADMIN);
        exigirTransicion(s, CollectionStatus.REJECTED);

        s.setClosedAt(LocalDateTime.now());
        s.setCloseReason(dto == null ? null : dto.reason());
        cambiarEstado(s, CollectionStatus.REJECTED, userId,
                dto == null ? null : dto.reason());

        CollectionRequestEntity guardada = requestRepository.save(s);
        eventProducer.publicarCerrada(guardada);
        return CollectionResponseDTO.fromEntity(guardada);
    }

    // =================================================================
    // LECTURA
    // =================================================================

    @Transactional(readOnly = true)
    public CollectionResponseDTO obtener(Long id, Long userId, String rol) {
        CollectionRequestEntity s = requestRepository.findByIdWithItems(id)
                .orElseThrow(() -> new CollectionNotFoundException(id));

        boolean puedeVerla = s.getRequesterId().equals(userId)
                || userId.equals(s.getCollectorId())
                || ROL_ADMIN.equals(rol)
                || ROL_COLLECTOR.equals(rol);

        if (!puedeVerla) {
            throw new ForbiddenOperationException("No tienes acceso a esta solicitud");
        }
        return CollectionResponseDTO.fromEntity(s);
    }

    @Transactional(readOnly = true)
    public Page<CollectionSummaryDTO> misSolicitudes(Long userId, Pageable pageable) {
        return requestRepository
                .findByRequesterIdOrderByCreatedAtDesc(userId, pageable)
                .map(CollectionSummaryDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<CollectionSummaryDTO> pendientes(Pageable pageable) {
        return requestRepository
                .findByStatusOrderByCreatedAtAsc(CollectionStatus.PENDING, pageable)
                .map(CollectionSummaryDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<CollectionSummaryDTO> buscar(CollectionStatus status, Long collectorId,
                                             Long requesterId, LocalDateTime desde,
                                             LocalDateTime hasta, Pageable pageable) {
        return requestRepository
                .buscarConFiltros(status, collectorId, requesterId, desde, hasta, pageable)
                .map(CollectionSummaryDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryResponseDTO> historial(Long id, Long userId, String rol) {
        CollectionRequestEntity s = buscarOFallar(id);
        if (!s.getRequesterId().equals(userId) && !ROL_ADMIN.equals(rol)) {
            throw new ForbiddenOperationException("No tienes acceso a esta solicitud");
        }
        return historyRepository.findByRequestIdOrderByChangedAtAsc(id).stream()
                .map(StatusHistoryResponseDTO::fromEntity)
                .toList();
    }

    // =================================================================
    // AYUDANTES PRIVADOS
    // =================================================================

    private CollectionRequestEntity buscarOFallar(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new CollectionNotFoundException(id));
    }

    /** La unica puerta para cambiar de estado: valida y deja rastro. */
    private void cambiarEstado(CollectionRequestEntity s, CollectionStatus destino,
                               Long userId, String motivo) {
        CollectionStatus anterior = s.getStatus();
        s.setStatus(destino);
        registrarHistorial(s, anterior, destino, userId, motivo);
    }

    private void registrarHistorial(CollectionRequestEntity s, CollectionStatus desde,
                                    CollectionStatus hacia, Long userId, String motivo) {
        historyRepository.save(CollectionStatusHistoryEntity.builder()
                .request(s)
                .fromStatus(desde)
                .toStatus(hacia)
                .changedByUserId(userId)
                .reason(motivo)
                .changedAt(LocalDateTime.now())
                .build());
    }

    /** Le pregunta al enum si la transicion es legal. Una sola fuente de verdad. */
    private void exigirTransicion(CollectionRequestEntity s, CollectionStatus destino) {
        if (!s.getStatus().puedeCambiarA(destino)) {
            throw new InvalidStatusTransitionException(s.getId(), s.getStatus(), destino);
        }
    }

    private void exigirRol(String rol, String mensaje, String... permitidos) {
        for (String p : permitidos) {
            if (p.equals(rol)) return;
        }
        throw new ForbiddenOperationException(mensaje);
    }

    private void exigirRecolectorAsignadoOAdmin(CollectionRequestEntity s,
                                                Long userId, String rol) {
        if (ROL_ADMIN.equals(rol)) return;
        if (userId.equals(s.getCollectorId())) return;
        throw new ForbiddenOperationException(
                "Solo el recolector asignado o un administrador puede hacer esto");
    }
}
