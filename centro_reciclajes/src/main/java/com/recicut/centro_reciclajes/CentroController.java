package com.recicut.centro_reciclajes;

import com.recicut.centro_reciclajes.entity.CentroReciclaje;
import com.recicut.centro_reciclajes.service.CentroReciclajeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centros")
@CrossOrigin(origins = "http://localhost:4200")
public class CentroController {

    private final CentroReciclajeService centroReciclajeService;

    public CentroController(CentroReciclajeService centroReciclajeService) {
        this.centroReciclajeService = centroReciclajeService;
    }

    @PostMapping
    public ResponseEntity<?> crearCentro(@RequestBody CentroReciclaje centro) {
        try {
            CentroReciclaje nuevo = centroReciclajeService.crearCentro(centro);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CentroReciclaje>> listarCentros() {
        return ResponseEntity.ok(centroReciclajeService.listarCentros());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCentro(@PathVariable Long id) {
        try {
            centroReciclajeService.eliminarCentro(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}