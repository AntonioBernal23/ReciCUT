package com.recicut.recolectores;

import com.recicut.recolectores.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recolectores")
public class RecolectorController {

    // Tu método original de prueba
    @GetMapping
    public List<String> listarRecolectores() {
        return List.of("Recolector 1 - Prueba", "Recolector 2 - Prueba");
    }

    // Método de prueba local con datos simulados
    @GetMapping("/usuario/{id}")
    public ResponseEntity<UserDto> obtenerUsuarioDeTony(@PathVariable("id") Long id) {
        UserDto usuarioPrueba = new UserDto();
        usuarioPrueba.setId(id);
        usuarioPrueba.setNombre("Usuario de Prueba");
        usuarioPrueba.setEmail("prueba@recicut.com");

        return ResponseEntity.ok(usuarioPrueba);
    }
}