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

    @PostMapping("create")
    public ResponseEntity<CampaniaEntity> create(
            @Valid @RequestBody CampaniaRequest campaniaRequest,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaniaService.create(campaniaRequest, userId));
    }

    @GetMapping("campanias")
    public ResponseEntity<List<CampaniaEntity>> campaniasList() {
        return ResponseEntity.ok(campaniaService.getCampanias());
    }

    @GetMapping("{id}")
    public ResponseEntity<CampaniaEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(campaniaService.getById(id));
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