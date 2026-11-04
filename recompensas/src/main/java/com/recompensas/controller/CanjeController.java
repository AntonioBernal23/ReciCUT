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

    // Consultar canjes de un usuario
    @GetMapping("/{usuarioId}")
    public List<Canje> consultarCanjes(@PathVariable Long usuarioId) {
        return canjeService.consultarCanjes(usuarioId);
    }

    // Realizar un canje
    @PostMapping("/{usuarioId}/{recompensaId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Canje realizarCanje(
            @PathVariable Long usuarioId,
            @PathVariable Long recompensaId) {

        return canjeService.realizarCanje(usuarioId, recompensaId);
    }

    // Aprobar un canje
    @PutMapping("/{canjeId}/aprobar")
    public Canje aprobarCanje(@PathVariable Long canjeId) {
        return canjeService.aprobarCanje(canjeId);
    }

    // Cancelar un canje
    @PutMapping("/{canjeId}/cancelar")
    public Canje cancelarCanje(@PathVariable Long canjeId) {
        return canjeService.cancelarCanje(canjeId);
    }

    // Entregar una recompensa
    @PutMapping("/{canjeId}/entregar")
    public Canje entregarCanje(@PathVariable Long canjeId) {
        return canjeService.entregarCanje(canjeId);
    }
}
