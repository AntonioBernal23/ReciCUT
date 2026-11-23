package com.recicut.centro_reciclajes.service;

import com.recicut.centro_reciclajes.entity.CentroReciclaje;
import com.recicut.centro_reciclajes.repository.CentroReciclajeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CentroReciclajeService {

    private final CentroReciclajeRepository centroReciclajeRepository;

    public CentroReciclajeService(CentroReciclajeRepository centroReciclajeRepository) {
        this.centroReciclajeRepository = centroReciclajeRepository;
    }

    public CentroReciclaje crearCentro(CentroReciclaje centro) {
        if (centro.getNombre() == null || centro.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del centro es obligatorio");
        }
        if (centro.getDireccion() == null || centro.getDireccion().isBlank()) {
            throw new IllegalArgumentException("La dirección es obligatoria");
        }
        return centroReciclajeRepository.save(centro);
    }

    public List<CentroReciclaje> listarCentros() {
        return centroReciclajeRepository.findAll();
    }

    public void eliminarCentro(Long id) {
        if (!centroReciclajeRepository.existsById(id)) {
            throw new IllegalArgumentException("Centro de reciclaje no encontrado con id: " + id);
        }
        centroReciclajeRepository.deleteById(id);
    }
}