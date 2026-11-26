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

    // Catalogo para usuarios: solo activas
    @GetMapping
    public List<RecompensaDTO> listarActivas() {
        return recompensaService.listarActivas().stream().map(this::convertirDTO).toList();
    }

    // Catalogo completo (administracion)
    @GetMapping("/todas")
    public List<RecompensaDTO> listarTodas(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirAdmin(role);
        return recompensaService.listarTodas().stream().map(this::convertirDTO).toList();
    }

    @GetMapping("/{id}")
    public RecompensaDTO buscarPorId(@PathVariable Long id) {
        return convertirDTO(recompensaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecompensaDTO crear(
            @RequestBody RecompensaDTO dto,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirAdmin(role);
        return convertirDTO(recompensaService.crear(aEntidad(dto)));
    }

    @PutMapping("/{id}")
    public RecompensaDTO actualizar(
            @PathVariable Long id,
            @RequestBody RecompensaDTO dto,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirAdmin(role);
        return convertirDTO(recompensaService.actualizar(id, aEntidad(dto)));
    }

    @PutMapping("/{id}/activar")
    public RecompensaDTO reactivar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirAdmin(role);
        return convertirDTO(recompensaService.reactivar(id));
    }

    // "Eliminar" = desactivar: se oculta del catalogo y se conserva el historial
    @DeleteMapping("/{id}")
    public RecompensaDTO desactivar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        Acceso.exigirAdmin(role);
        return convertirDTO(recompensaService.desactivar(id));
    }

    private Recompensa aEntidad(RecompensaDTO dto) {
        Recompensa r = new Recompensa();
        r.setNombre(dto.getNombre());
        r.setDescripcion(dto.getDescripcion());
        r.setCostoPuntos(dto.getCostoPuntos());
        r.setStock(dto.getStock());
        return r;
    }

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