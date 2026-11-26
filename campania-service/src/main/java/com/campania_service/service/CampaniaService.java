package com.campania_service.service;

import com.campania_service.dto.CampaniaRequest;
import com.campania_service.entity.CampaniaEntity;
import com.campania_service.repository.CampaniaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@Service
public class CampaniaService {

    private final CampaniaRepository campaniaRepository;

    public CampaniaService(CampaniaRepository campaniaRepository) {
        this.campaniaRepository = campaniaRepository;
    }

    public CampaniaEntity create(CampaniaRequest request, Long userId) {

        if (request.getDateStart() != null && request.getEndDate() != null
                && request.getEndDate().before(request.getDateStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser antes de la fecha de inicio");
        }

        CampaniaEntity campania = new CampaniaEntity();
        campania.setUserId(userId);
        campania.setName(request.getName());
        campania.setType(request.getType());
        campania.setEstado(request.getEstado() != null
                ? request.getEstado()
                : CampaniaEntity.Estado.BORRADOR);
        campania.setDateStart(request.getDateStart());
        campania.setEndDate(request.getEndDate());
        campania.setPuntos(request.getPuntos() == null ? 0 : request.getPuntos());
        campania.setCreatedAt(new Date());

        return campaniaRepository.save(campania);
    }

    public List<CampaniaEntity> getCampanias(boolean esAdmin) {
        expirarVencidas();
        return campaniaRepository.findAll().stream()
                .filter(c -> esAdmin || c.getEstado() != CampaniaEntity.Estado.BORRADOR)
                .toList();
    }

    public CampaniaEntity getById(Long id) {
        return campaniaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Campa\u00f1a no encontrada"));
    }

    public CampaniaEntity getVisibleById(Long id, boolean esAdmin) {
        CampaniaEntity campania = getById(id);
        if (!esAdmin && campania.getEstado() == CampaniaEntity.Estado.BORRADOR) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Campa\u00f1a no encontrada");
        }
        return campania;
    }

    public CampaniaEntity activar(Long id) {
        CampaniaEntity campania = getById(id);
        if (campania.getEstado() != CampaniaEntity.Estado.BORRADOR
                && campania.getEstado() != CampaniaEntity.Estado.CREADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden activar campa\u00f1as en borrador o creadas");
        }
        if (campania.getEndDate() != null && campania.getEndDate().before(new Date())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La campa\u00f1a ya termin\u00f3 y no se puede activar");
        }
        campania.setEstado(CampaniaEntity.Estado.ACTIVA);
        return campaniaRepository.save(campania);
    }

    public CampaniaEntity cerrar(Long id) {
        CampaniaEntity campania = getById(id);
        if (campania.getEstado() != CampaniaEntity.Estado.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden cerrar campa\u00f1as activas");
        }
        campania.setEstado(CampaniaEntity.Estado.VENCIDA);
        return campaniaRepository.save(campania);
    }

    public void eliminar(Long id) {
        CampaniaEntity campania = getById(id);
        if (campania.getEstado() == CampaniaEntity.Estado.ACTIVA
                || campania.getEstado() == CampaniaEntity.Estado.VENCIDA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden eliminar campa\u00f1as en borrador o creadas. Cierra la campa\u00f1a en su lugar");
        }
        try {
            campaniaRepository.delete(campania);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La campa\u00f1a tiene inscripciones y no se puede eliminar");
        }
    }

    private void expirarVencidas() {
        Date ahora = new Date();
        for (CampaniaEntity c : campaniaRepository.findAll()) {
            if (c.getEstado() == CampaniaEntity.Estado.ACTIVA
                    && c.getEndDate() != null && c.getEndDate().before(ahora)) {
                c.setEstado(CampaniaEntity.Estado.VENCIDA);
                campaniaRepository.save(c);
            }
        }
    }
}