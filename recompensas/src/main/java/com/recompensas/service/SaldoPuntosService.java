package com.recompensas.service;

import com.recompensas.entity.SaldoPuntos;
import com.recompensas.repository.SaldoPuntosRepository;
import org.springframework.stereotype.Service;

@Service
public class SaldoPuntosService {

    private final SaldoPuntosRepository saldoPuntosRepository;

    public SaldoPuntosService(SaldoPuntosRepository saldoPuntosRepository) {
        this.saldoPuntosRepository = saldoPuntosRepository;
    }

    // Consultar el saldo de puntos de un usuario
    public SaldoPuntos consultarSaldo(Long usuarioId) {

        return saldoPuntosRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearSaldo(usuarioId));
    }

    // Crear un saldo inicial para un usuario
    public SaldoPuntos crearSaldo(Long usuarioId) {

        if (usuarioId == null) {
            throw new RuntimeException("El ID del usuario es obligatorio");
        }

        SaldoPuntos saldo = new SaldoPuntos();
        saldo.setUsuarioId(usuarioId);
        saldo.setPuntos(0);

        return saldoPuntosRepository.save(saldo);
    }
}