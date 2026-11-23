package com.recicut.recolectores;

import com.recicut.recolectores.entity.Recolector;
import com.recicut.recolectores.service.RecolectorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recolectores")
@CrossOrigin(origins = "http://localhost:4200")
public class RecolectorController {

    private final RecolectorService recolectorService;

    public RecolectorController(RecolectorService recolectorService) {
        this.recolectorService = recolectorService;
    }

    @PostMapping
    public ResponseEntity<?> crearRecolector(@RequestBody Recolector recolector) {
        try {
            Recolector nuevo = recolectorService.crearRecolector(recolector);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Recolector>> listarRecolectores() {
        return ResponseEntity.ok(recolectorService.listarRecolectores());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarRecolector(@PathVariable Long id) {
        try {
            recolectorService.eliminarRecolector(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}