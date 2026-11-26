package com.recompensas.service;

import com.recompensas.entity.MovimientoPuntos;
import com.recompensas.entity.SaldoPuntos;
import com.recompensas.repository.MovimientoPuntosRepository;
import com.recompensas.repository.SaldoPuntosRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SaldoPuntosService {

    private final SaldoPuntosRepository saldoPuntosRepository;
    private final MovimientoPuntosRepository movimientoPuntosRepository;

    public SaldoPuntosService(SaldoPuntosRepository saldoPuntosRepository,
                              MovimientoPuntosRepository movimientoPuntosRepository) {
        this.saldoPuntosRepository = saldoPuntosRepository;
        this.movimientoPuntosRepository = movimientoPuntosRepository;
    }

    // Consultar el saldo de puntos de un usuario
    public SaldoPuntos consultarSaldo(Long usuarioId) {
        return saldoPuntosRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearSaldo(usuarioId));
    }

    // Crear un saldo inicial para un usuario
    public SaldoPuntos crearSaldo(Long usuarioId) {
        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del usuario es obligatorio");
        }
        SaldoPuntos saldo = new SaldoPuntos();
        saldo.setUsuarioId(usuarioId);
        saldo.setPuntos(0);
        return saldoPuntosRepository.save(saldo);
    }

    // Sumar puntos a un usuario. Si la referencia ya se acredito antes, no suma otra vez.
    @Transactional
    public SaldoPuntos acreditar(Long usuarioId, Integer cantidad, String referencia, String descripcion) {
        if (usuarioId == null || cantidad == null || cantidad <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El usuario y una cantidad mayor a cero son obligatorios");
        }
        SaldoPuntos saldo = consultarSaldo(usuarioId);
        boolean hayReferencia = referencia != null && !referencia.isBlank();
        if (hayReferencia && movimientoPuntosRepository.existsByUsuarioIdAndReferencia(usuarioId, referencia)) {
            return saldo;
        }
        saldo.setPuntos(saldo.getPuntos() + cantidad);
        saldoPuntosRepository.save(saldo);

        MovimientoPuntos movimiento = new MovimientoPuntos();
        movimiento.setUsuarioId(usuarioId);
        movimiento.setCantidad(cantidad);
        movimiento.setTipo(hayReferencia && referencia.startsWith("CAMPANIA-") ? "CAMPANIA" : "ACREDITACION");
        String texto = descripcion == null ? "Puntos acreditados" : descripcion;
        movimiento.setDescripcion(texto.length() > 255 ? texto.substring(0, 255) : texto);
        movimiento.setReferencia(hayReferencia ? referencia : null);
        movimientoPuntosRepository.save(movimiento);
        return saldo;
    }
}