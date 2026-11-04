package com.recompensas.service;

import com.recompensas.entity.Recompensa;
import com.recompensas.repository.RecompensaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecompensaService {

    private final RecompensaRepository recompensaRepository;

    public RecompensaService(RecompensaRepository recompensaRepository) {
        this.recompensaRepository = recompensaRepository;
    }

    // Listar todas las recompensas activas
    public List<Recompensa> listarActivas() {
        return recompensaRepository.findByActivaTrue();
    }

    // Listar todas las recompensas
    public List<Recompensa> listarTodas() {
        return recompensaRepository.findAll();
    }

    // Buscar una recompensa por su ID
    public Recompensa buscarPorId(Long id) {
        return recompensaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró la recompensa con ID: " + id
                ));
    }

    // Crear una recompensa
    public Recompensa crear(Recompensa recompensa) {

        if (recompensa.getNombre() == null || recompensa.getNombre().isBlank()) {
            throw new RuntimeException("El nombre es obligatorio");
        }

        if (recompensa.getCostoPuntos() == null || recompensa.getCostoPuntos() <= 0) {
            throw new RuntimeException("El costo debe ser mayor a cero");
        }

        if (recompensa.getStock() == null || recompensa.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        recompensa.setActiva(true);

        return recompensaRepository.save(recompensa);
    }

    // Actualizar una recompensa
    public Recompensa actualizar(Long id, Recompensa datos) {

        Recompensa recompensa = buscarPorId(id);

        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new RuntimeException("El nombre es obligatorio");
        }

        if (datos.getCostoPuntos() == null || datos.getCostoPuntos() <= 0) {
            throw new RuntimeException("El costo debe ser mayor a cero");
        }

        if (datos.getStock() == null || datos.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        recompensa.setNombre(datos.getNombre());
        recompensa.setDescripcion(datos.getDescripcion());
        recompensa.setCostoPuntos(datos.getCostoPuntos());
        recompensa.setStock(datos.getStock());
        recompensa.setActiva(true);

        return recompensaRepository.save(recompensa);
    }

    // Desactivar una recompensa
    public Recompensa desactivar(Long id) {

        Recompensa recompensa = buscarPorId(id);
        recompensa.setActiva(false);

        return recompensaRepository.save(recompensa);
    }
}

