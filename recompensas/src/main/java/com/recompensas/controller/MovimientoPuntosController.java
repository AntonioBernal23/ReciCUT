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

    // Consultar historial de movimientos
    @GetMapping("/{usuarioId}")
    public List<MovimientoPuntos> consultarHistorial(@PathVariable Long usuarioId) {
        return movimientoPuntosService.consultarHistorial(usuarioId);
    }
}