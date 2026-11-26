package com.recompensas.controller;

import com.recompensas.entity.SaldoPuntos;
import com.recompensas.service.SaldoPuntosService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recompensas/saldo")
public class SaldoPuntosController {

    private final SaldoPuntosService saldoPuntosService;

    public SaldoPuntosController(SaldoPuntosService saldoPuntosService) {
        this.saldoPuntosService = saldoPuntosService;
    }

    @GetMapping("/{usuarioId}")
    public SaldoPuntos consultarSaldo(
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-User-Id", required = false) Long solicitante,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirPropietarioOAdmin(role, solicitante, usuarioId);
        return saldoPuntosService.consultarSaldo(usuarioId);
    }
}