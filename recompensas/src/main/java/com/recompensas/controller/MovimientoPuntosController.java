package com.recompensas.controller;

import com.recompensas.entity.MovimientoPuntos;
import com.recompensas.service.MovimientoPuntosService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recompensas/movimientos")
public class MovimientoPuntosController {

    private final MovimientoPuntosService movimientoPuntosService;

    public MovimientoPuntosController(MovimientoPuntosService movimientoPuntosService) {
        this.movimientoPuntosService = movimientoPuntosService;
    }

    @GetMapping("/{usuarioId}")
    public List<MovimientoPuntos> consultarHistorial(
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-User-Id", required = false) Long solicitante,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirPropietarioOAdmin(role, solicitante, usuarioId);
        return movimientoPuntosService.consultarHistorial(usuarioId);
    }
}