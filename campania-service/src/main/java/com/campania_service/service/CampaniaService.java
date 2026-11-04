package com.campania_service.service;

import com.campania_service.dto.CampaniaRequest;
import com.campania_service.entity.CampaniaEntity;
import com.campania_service.repository.CampaniaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
//aquiiiiiiiiiiiiiiiiiiiiii!//
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
        campania.setCreatedAt(new Date());

        return campaniaRepository.save(campania);
    }

    public List<CampaniaEntity> getCampanias() {
        return campaniaRepository.findAll();
    }

    public CampaniaEntity getById(Long id) {
        return campaniaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Campaña no encontrada"));
    }
}