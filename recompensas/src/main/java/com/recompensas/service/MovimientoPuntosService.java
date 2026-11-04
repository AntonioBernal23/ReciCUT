package com.recompensas.service;

import com.recompensas.entity.MovimientoPuntos;
import com.recompensas.repository.MovimientoPuntosRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimientoPuntosService {

    private final MovimientoPuntosRepository movimientoPuntosRepository;

    public MovimientoPuntosService(MovimientoPuntosRepository movimientoPuntosRepository) {
        this.movimientoPuntosRepository = movimientoPuntosRepository;
    }

    // Consultar el historial de movimientos de un usuario
    public List<MovimientoPuntos> consultarHistorial(Long usuarioId) {

        if (usuarioId == null) {
            throw new RuntimeException("El ID del usuario es obligatorio");
        }

        return movimientoPuntosRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }
}