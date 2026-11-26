package com.recompensas.controller;

import com.recompensas.entity.SaldoPuntos;
import com.recompensas.service.SaldoPuntosService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/puntos")
public class PuntosInternosController {

    private final SaldoPuntosService saldoPuntosService;

    public PuntosInternosController(SaldoPuntosService saldoPuntosService) {
        this.saldoPuntosService = saldoPuntosService;
    }

    // Ruta interna entre servicios: el Gateway no la expone
    @PostMapping
    public SaldoPuntos acreditar(@RequestBody Map<String, Object> body) {
        Long usuarioId = body.get("usuarioId") == null ? null : ((Number) body.get("usuarioId")).longValue();
        Integer cantidad = body.get("cantidad") == null ? null : ((Number) body.get("cantidad")).intValue();
        String referencia = (String) body.get("referencia");
        String descripcion = (String) body.get("descripcion");
        return saldoPuntosService.acreditar(usuarioId, cantidad, referencia, descripcion);
    }
}