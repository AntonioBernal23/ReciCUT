package com.campania_service.controller;

import com.campania_service.dto.CampaniaRequest;
import com.campania_service.entity.CampaniaEntity;
import com.campania_service.service.CampaniaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campania")
public class CampaniaController {
    private final CampaniaService campaniaService;

    private CampaniaController(CampaniaService campaniaService){
        this.campaniaService = campaniaService;
    }

    @PostMapping("create")
    public ResponseEntity<CampaniaEntity> create(@RequestBody CampaniaRequest campaniaRequest) {
        return campaniaService.create(campaniaRequest);
    }

    @GetMapping("campanias")
    public ResponseEntity<List<CampaniaEntity>> campaniasList() {
        return campaniaService.getCampanias();
    }

}
