package com.recompensas.service;

import com.recompensas.entity.Recompensa;
import com.recompensas.repository.RecompensaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecompensaService {

    private final RecompensaRepository recompensaRepository;

    public RecompensaService(RecompensaRepository recompensaRepository) {
        this.recompensaRepository = recompensaRepository;
    }

    public List<Recompensa> listarActivas() {
        return recompensaRepository.findByActivaTrue();
    }

    public List<Recompensa> listarTodas() {
        return recompensaRepository.findAll();
    }

    public Recompensa buscarPorId(Long id) {
        return recompensaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontr\u00f3 la recompensa con ID: " + id));
    }

    private void validar(Recompensa r) {
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (r.getCostoPuntos() == null || r.getCostoPuntos() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El costo debe ser mayor a cero");
        }
        if (r.getStock() == null || r.getStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock no puede ser negativo");
        }
    }

    public Recompensa crear(Recompensa recompensa) {
        validar(recompensa);
        recompensa.setActiva(true);
        return recompensaRepository.save(recompensa);
    }

    public Recompensa actualizar(Long id, Recompensa datos) {
        Recompensa recompensa = buscarPorId(id);
        validar(datos);
        recompensa.setNombre(datos.getNombre());
        recompensa.setDescripcion(datos.getDescripcion());
        recompensa.setCostoPuntos(datos.getCostoPuntos());
        recompensa.setStock(datos.getStock());
        return recompensaRepository.save(recompensa);
    }

    public Recompensa desactivar(Long id) {
        Recompensa recompensa = buscarPorId(id);
        recompensa.setActiva(false);
        return recompensaRepository.save(recompensa);
    }

    public Recompensa reactivar(Long id) {
        Recompensa recompensa = buscarPorId(id);
        recompensa.setActiva(true);
        return recompensaRepository.save(recompensa);
    }
}