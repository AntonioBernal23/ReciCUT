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
        inscripcion.setUsuarioEmail(emailDeLaPeticion());
        inscripcion.setFechaInscripcion(new Date());

        return inscripcionRepository.save(inscripcion);
    }

    public void cancelar(Long campaniaId, Long usuarioId) {
        InscripcionEntity inscripcion = inscripcionRepository
                .findByCampaniaIdAndUsuarioId(campaniaId, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No estás inscrito en esta campaña"));
        if (Boolean.TRUE.equals(inscripcion.getCompletada())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes cancelar una participaci\u00f3n que ya fue completada");
        }
        inscripcionRepository.delete(inscripcion);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.campania_service.client.PuntosClient puntosClient;

    private String emailDeLaPeticion() {
        try {
            org.springframework.web.context.request.RequestAttributes attrs =
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
                return sra.getRequest().getHeader("X-User-Email");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public List<InscripcionEntity> inscripcionesDeCampania(Long campaniaId) {
        campaniaService.getById(campaniaId);
        return inscripcionRepository.findAll().stream()
                .filter(i -> i.getCampania() != null && campaniaId.equals(i.getCampania().getId()))
                .toList();
    }

    public InscripcionEntity completar(Long campaniaId, Long usuarioId) {
        CampaniaEntity campania = campaniaService.getById(campaniaId);
        InscripcionEntity inscripcion = inscripcionRepository
                .findByCampaniaIdAndUsuarioId(campaniaId, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El usuario no est\u00e1 inscrito en esta campa\u00f1a"));
        if (Boolean.TRUE.equals(inscripcion.getCompletada())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La participaci\u00f3n ya fue marcada como completada");
        }
        int puntos = campania.getPuntos() == null ? 0 : campania.getPuntos();
        if (puntos > 0) {
            puntosClient.acreditar(usuarioId, puntos,
                    "CAMPANIA-" + campaniaId + "-USUARIO-" + usuarioId,
                    "Puntos por completar la campa\u00f1a: " + campania.getName());
        }
        inscripcion.setCompletada(true);
        inscripcion.setFechaCompletada(new Date());
        return inscripcionRepository.save(inscripcion);
    }

    public List<InscripcionEntity> misInscripciones(Long usuarioId) {
        return inscripcionRepository.findByUsuarioId(usuarioId);
    }
}