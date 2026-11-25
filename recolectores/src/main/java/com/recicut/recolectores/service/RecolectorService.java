package com.recicut.recolectores.service;

import com.recicut.recolectores.client.UserClient;
import com.recicut.recolectores.dto.UserDto;
import com.recicut.recolectores.entity.Recolector;
import com.recicut.recolectores.repository.RecolectorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecolectorService {

    private final RecolectorRepository recolectorRepository;
    private final UserClient userClient;

    public RecolectorService(RecolectorRepository recolectorRepository, UserClient userClient) {
        this.recolectorRepository = recolectorRepository;
        this.userClient = userClient;
    }

    public Recolector crearRecolector(Recolector recolector) {
        if (recolector.getNombre() == null || recolector.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del recolector es obligatorio");
        }
        if (recolector.getZona() == null || recolector.getZona().isBlank()) {
            throw new IllegalArgumentException("La zona del recolector es obligatoria");
        }

        if (recolector.getUsuarioId() != null && userClient != null) {
            try {
                UserDto usuario = userClient.getUserById(recolector.getUsuarioId());
                if (usuario == null) {
                    throw new IllegalArgumentException("El usuario indicado no existe");
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("No se pudo validar el usuario con user-service: " + e.getMessage());
            }
        }

        return recolectorRepository.save(recolector);
    }

    public List<Recolector> listarRecolectores() {
        return recolectorRepository.findAll();
    }

    public void eliminarRecolector(Long id) {
        if (!recolectorRepository.existsById(id)) {
            throw new IllegalArgumentException("Recolector no encontrado con id: " + id);
        }
        recolectorRepository.deleteById(id);
    }
}