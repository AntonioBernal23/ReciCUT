package com.recompensas.controller;

import com.recompensas.entity.Canje;
import com.recompensas.service.CanjeService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recompensas/canjes")
public class CanjeController {

    private final CanjeService canjeService;

    public CanjeController(CanjeService canjeService) {
        this.canjeService = canjeService;
    }

    // Todos los canjes (administracion)
    @GetMapping
    public List<Canje> todos(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirAdmin(role);
        return canjeService.todosLosCanjes();
    }

    // Canjes de un usuario: solo el propio usuario o un administrador
    @GetMapping("/{usuarioId}")
    public List<Canje> consultarCanjes(
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-User-Id", required = false) Long solicitante,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirPropietarioOAdmin(role, solicitante, usuarioId);
        return canjeService.consultarCanjes(usuarioId);
    }

    // Realizar un canje: solo con la propia cuenta
    @PostMapping("/{usuarioId}/{recompensaId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Canje realizarCanje(
            @PathVariable Long usuarioId,
            @PathVariable Long recompensaId,
            @RequestHeader(value = "X-User-Id", required = false) Long solicitante) {
        Acceso.exigirPropietario(solicitante, usuarioId);
        return canjeService.realizarCanje(usuarioId, recompensaId);
    }

    @PutMapping("/{canjeId}/aprobar")
    public Canje aprobarCanje(
            @PathVariable Long canjeId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirAdmin(role);
        return canjeService.aprobarCanje(canjeId);
    }

    @PutMapping("/{canjeId}/cancelar")
    public Canje cancelarCanje(
            @PathVariable Long canjeId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirAdmin(role);
        return canjeService.cancelarCanje(canjeId);
    }

    @PutMapping("/{canjeId}/entregar")
    public Canje entregarCanje(
            @PathVariable Long canjeId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirAdmin(role);
        return canjeService.entregarCanje(canjeId);
    }
}