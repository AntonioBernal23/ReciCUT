package com.recicut.recolectores;

import com.recicut.recolectores.entity.Recolector;
import com.recicut.recolectores.repository.RecolectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recolectores")
@CrossOrigin(origins = "http://localhost:4200")
public class RecolectorController {

    @Autowired
    private RecolectorRepository recolectorRepository;

    // Endpoint para guardar un nuevo recolector en la BD
    @PostMapping
    public ResponseEntity<Recolector> crearRecolector(@RequestBody Recolector recolector) {
        Recolector nuevo = recolectorRepository.save(recolector);
        return ResponseEntity.ok(nuevo);
    }

    // Endpoint para listar todos los recolectores desde la BD
    @GetMapping
    public ResponseEntity<List<Recolector>> listarRecolectores() {
        return ResponseEntity.ok(recolectorRepository.findAll());
    }
}