package com.campania_service.controller;

import com.campania_service.dto.CampaniaRequest;
import com.campania_service.entity.CampaniaEntity;
import com.campania_service.entity.InscripcionEntity;
import com.campania_service.service.CampaniaService;
import com.campania_service.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/campania")
public class CampaniaController {

    private final CampaniaService campaniaService;
    private final InscripcionService inscripcionService;

    public CampaniaController(CampaniaService campaniaService,
                              InscripcionService inscripcionService) {
        this.campaniaService = campaniaService;
        this.inscripcionService = inscripcionService;
    }

    private static boolean esAdmin(String role) {
        return "ADMIN".equals(role);
    }

    private static void exigirAdmin(String role) {
        if (!esAdmin(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un administrador puede realizar esta acci\u00f3n");
        }
    }

    @PostMapping("create")
    public ResponseEntity<CampaniaEntity> create(
            @Valid @RequestBody CampaniaRequest campaniaRequest,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        exigirAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaniaService.create(campaniaRequest, userId));
    }

    @GetMapping("campanias")
    public ResponseEntity<List<CampaniaEntity>> campaniasList(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(campaniaService.getCampanias(esAdmin(role)));
    }

    @GetMapping("{id}")
    public ResponseEntity<CampaniaEntity> getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(campaniaService.getVisibleById(id, esAdmin(role)));
    }

    @PutMapping("{id}/activar")
    public ResponseEntity<CampaniaEntity> activar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        exigirAdmin(role);
        return ResponseEntity.ok(campaniaService.activar(id));
    }

    @PutMapping("{id}/cerrar")
    public ResponseEntity<CampaniaEntity> cerrar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        exigirAdmin(role);
        return ResponseEntity.ok(campaniaService.cerrar(id));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        exigirAdmin(role);
        campaniaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{campaniaId}/inscripciones")
    public ResponseEntity<List<InscripcionEntity>> participantes(
            @PathVariable Long campaniaId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        exigirAdmin(role);
        return ResponseEntity.ok(inscripcionService.inscripcionesDeCampania(campaniaId));
    }

    @PutMapping("{campaniaId}/inscripciones/{usuarioId}/completar")
    public ResponseEntity<InscripcionEntity> completar(
            @PathVariable Long campaniaId,
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        exigirAdmin(role);
        return ResponseEntity.ok(inscripcionService.completar(campaniaId, usuarioId));
    }

    @PostMapping("{campaniaId}/inscripciones")
    public ResponseEntity<InscripcionEntity> inscribir(
            @PathVariable Long campaniaId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscripcionService.inscribir(campaniaId, userId));
    }

    @DeleteMapping("{campaniaId}/inscripciones")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long campaniaId,
            @RequestHeader("X-User-Id") Long userId) {
        inscripcionService.cancelar(campaniaId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("mis-inscripciones")
    public ResponseEntity<List<InscripcionEntity>> misInscripciones(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(inscripcionService.misInscripciones(userId));
    }
}