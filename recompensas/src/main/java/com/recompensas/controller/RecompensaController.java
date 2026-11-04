package com.recompensas.controller;

import com.recompensas.dto.RecompensaDTO;
import com.recompensas.entity.Recompensa;
import com.recompensas.service.RecompensaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recompensas")
public class RecompensaController {

    private final RecompensaService recompensaService;

    public RecompensaController(RecompensaService recompensaService) {
        this.recompensaService = recompensaService;
    }

    // Listar recompensas activas
    @GetMapping
    public List<RecompensaDTO> listarActivas() {
        return recompensaService.listarActivas()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Listar todas las recompensas
    @GetMapping("/todas")
    public List<RecompensaDTO> listarTodas() {
        return recompensaService.listarTodas()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Buscar recompensa por ID
    @GetMapping("/{id}")
    public RecompensaDTO buscarPorId(@PathVariable Long id) {
        return convertirDTO(recompensaService.buscarPorId(id));
    }

    // Crear recompensa
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecompensaDTO crear(@RequestBody RecompensaDTO dto) {

        Recompensa recompensa = new Recompensa();

        recompensa.setNombre(dto.getNombre());
        recompensa.setDescripcion(dto.getDescripcion());
        recompensa.setCostoPuntos(dto.getCostoPuntos());
        recompensa.setStock(dto.getStock());

        return convertirDTO(recompensaService.crear(recompensa));
    }

    // Actualizar recompensa
    @PutMapping("/{id}")
    public RecompensaDTO actualizar(
            @PathVariable Long id,
            @RequestBody RecompensaDTO dto) {

        Recompensa datos = new Recompensa();

        datos.setNombre(dto.getNombre());
        datos.setDescripcion(dto.getDescripcion());
        datos.setCostoPuntos(dto.getCostoPuntos());
        datos.setStock(dto.getStock());

        return convertirDTO(recompensaService.actualizar(id, datos));
    }

    // Desactivar recompensa
    @DeleteMapping("/{id}")
    public RecompensaDTO desactivar(@PathVariable Long id) {
        return convertirDTO(recompensaService.desactivar(id));
    }

    // Convertir entidad a DTO
    private RecompensaDTO convertirDTO(Recompensa recompensa) {

        RecompensaDTO dto = new RecompensaDTO();

        dto.setId(recompensa.getId());
        dto.setNombre(recompensa.getNombre());
        dto.setDescripcion(recompensa.getDescripcion());
        dto.setCostoPuntos(recompensa.getCostoPuntos());
        dto.setStock(recompensa.getStock());
        dto.setActiva(recompensa.getActiva());

        return dto;
    }
}
