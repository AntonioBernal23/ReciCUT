package com.campania_service.service;

import com.campania_service.dto.CampaniaRequest;
import com.campania_service.entity.CampaniaEntity;
import com.campania_service.repository.CampaniaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CampaniaService {

    private final CampaniaRepository campaniaRepository;

    public CampaniaService(CampaniaRepository campaniaRepository) {
        this.campaniaRepository = campaniaRepository;
    }

    public ResponseEntity<CampaniaEntity> create(CampaniaRequest campaniaRequest) {

        CampaniaEntity campaniaEntity = new CampaniaEntity();

        campaniaEntity.setUserId(campaniaRequest.getUserId());
        campaniaEntity.setName(campaniaRequest.getName());
        campaniaEntity.setType(campaniaRequest.getType());
        campaniaEntity.setEstado(campaniaRequest.getEstado());
        campaniaEntity.setDateStart(campaniaRequest.getDateStart());
        campaniaEntity.setEndDate(campaniaRequest.getEndDate());
        campaniaEntity.setCreatedAt(new Date());

        CampaniaEntity campaniaGuardada = campaniaRepository.save(campaniaEntity);

        return ResponseEntity.ok().body(campaniaGuardada);
    }

    public ResponseEntity<List<CampaniaEntity>> getCampanias() {

        List<CampaniaEntity> campaniasList = new ArrayList<>();
        campaniasList = campaniaRepository.findAll();

        return ResponseEntity.ok().body(campaniasList);

    }

}
