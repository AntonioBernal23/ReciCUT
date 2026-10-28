package com.collection_service.controller;

import com.collection_service.dto.*;
import com.collection_service.enums.CollectionStatus;
import com.collection_service.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/**
 * La puerta HTTP del servicio.
 *
 * No tiene logica de negocio: recibe, delega al service y devuelve.
 *
 * La identidad del usuario llega en los headers X-User-Id y X-User-Role, que
 * inyecta el api-gateway DESPUES de validar el JWT. Por eso aqui no se valida
 * ningun token: si este codigo se ejecuta, el gateway ya autentico.
 *
 * API basada en ACCIONES, no en un PATCH generico de status. En vez de
 * "PATCH /{id} {status: COMPLETED}" (que aceptaria cualquier cosa), hay un
 * endpoint por accion y cada uno aplica sus propias reglas.
 */
@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    // ---------------------------------------------------------------
    // Crear y editar
    // ---------------------------------------------------------------

    @PostMapping
    public ResponseEntity<CollectionResponseDTO> crear(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateCollectionRequestDTO dto) {

        CollectionResponseDTO creada = collectionService.crear(userId, dto);
        return ResponseEntity
                .created(URI.create("/api/collections/" + creada.id()))
                .body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateCollectionRequestDTO dto) {

        return ResponseEntity.ok(collectionService.actualizar(id, userId, dto));
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> obtener(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String rol) {

        return ResponseEntity.ok(collectionService.obtener(id, userId, rol));
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<CollectionSummaryDTO>> misSolicitudes(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20, sort = "createdAt",
                             direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(collectionService.misSolicitudes(userId, pageable));
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<CollectionSummaryDTO>> pendientes(
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(collectionService.pendientes(pageable));
    }

    @GetMapping
    public ResponseEntity<Page<CollectionSummaryDTO>> buscar(
            @RequestParam(required = false) CollectionStatus status,
            @RequestParam(required = false) Long collectorId,
            @RequestParam(required = false) Long requesterId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(collectionService.buscar(
                status, collectorId, requesterId, desde, hasta, pageable));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<StatusHistoryResponseDTO>> historial(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String rol) {

        return ResponseEntity.ok(collectionService.historial(id, userId, rol));
    }

    // ---------------------------------------------------------------
    // Acciones del ciclo de vida
    // ---------------------------------------------------------------

    @PatchMapping("/{id}/assign")
    public ResponseEntity<CollectionResponseDTO> asignar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @Valid @RequestBody AssignCollectorDTO dto) {

        return ResponseEntity.ok(collectionService.asignar(id, userId, rol, dto));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<CollectionResponseDTO> iniciar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String rol) {

        return ResponseEntity.ok(collectionService.iniciar(id, userId, rol));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<CollectionResponseDTO> completar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @Valid @RequestBody CompleteCollectionDTO dto) {

        return ResponseEntity.ok(collectionService.completar(id, userId, rol, dto));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<CollectionResponseDTO> cancelar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @RequestBody(required = false) CloseCollectionDTO dto) {

        return ResponseEntity.ok(collectionService.cancelar(id, userId, rol, dto));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<CollectionResponseDTO> rechazar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @RequestBody(required = false) CloseCollectionDTO dto) {

        return ResponseEntity.ok(collectionService.rechazar(id, userId, rol, dto));
    }
}
