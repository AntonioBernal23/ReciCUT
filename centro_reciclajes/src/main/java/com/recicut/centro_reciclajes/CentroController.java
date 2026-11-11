package com.recicut.centro_reciclajes;

import com.recicut.centro_reciclajes.entity.CentroReciclaje;
import com.recicut.centro_reciclajes.repository.CentroReciclajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centros")
@CrossOrigin(origins = "http://localhost:4200")
public class CentroController {

    @Autowired
    private CentroReciclajeRepository centroReciclajeRepository;

    @PostMapping
    public ResponseEntity<CentroReciclaje> crearCentro(@RequestBody CentroReciclaje centro) {
        CentroReciclaje nuevo = centroReciclajeRepository.save(centro);
        return ResponseEntity.ok(nuevo);
    }

    @GetMapping
    public ResponseEntity<List<CentroReciclaje>> listarCentros() {
        return ResponseEntity.ok(centroReciclajeRepository.findAll());
    }
}