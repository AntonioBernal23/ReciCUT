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

    // Consultar saldo de un usuario
    @GetMapping("/{usuarioId}")
    public SaldoPuntos consultarSaldo(@PathVariable Long usuarioId) {
        return saldoPuntosService.consultarSaldo(usuarioId);
    }
}
