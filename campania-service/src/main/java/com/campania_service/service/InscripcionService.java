package com.campania_service.service;

import com.campania_service.entity.CampaniaEntity;
import com.campania_service.entity.InscripcionEntity;
import com.campania_service.repository.InscripcionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final CampaniaService campaniaService;

    public InscripcionService(InscripcionRepository inscripcionRepository,
                              CampaniaService campaniaService) {
        this.inscripcionRepository = inscripcionRepository;
        this.campaniaService = campaniaService;
    }

    public InscripcionEntity inscribir(Long campaniaId, Long usuarioId) {

        CampaniaEntity campania = campaniaService.getById(campaniaId);

        if (campania.getEstado() != CampaniaEntity.Estado.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La campaña no está activa");
        }

        Date hoy = new Date();
        if (campania.getDateStart() != null && hoy.before(campania.getDateStart())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La campaña todavía no inicia");
        }
        if (campania.getEndDate() != null && hoy.after(campania.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La campaña ya terminó");
        }

        if (inscripcionRepository.existsByCampaniaIdAndUsuarioId(campaniaId, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya estás inscrito en esta campaña");
        }

        InscripcionEntity inscripcion = new InscripcionEntity();
        inscripcion.setCampania(campania);
        inscripcion.setUsuarioId(usuarioId);
        inscripcion.setFechaInscripcion(new Date());

        return inscripcionRepository.save(inscripcion);
    }

    public void cancelar(Long campaniaId, Long usuarioId) {
        InscripcionEntity inscripcion = inscripcionRepository
                .findByCampaniaIdAndUsuarioId(campaniaId, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No estás inscrito en esta campaña"));
        inscripcionRepository.delete(inscripcion);
    }

    public List<InscripcionEntity> misInscripciones(Long usuarioId) {
        return inscripcionRepository.findByUsuarioId(usuarioId);
    }
}