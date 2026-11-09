package com.recicut.recolectores;

import com.recicut.recolectores.client.UserClient;
import com.recicut.recolectores.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recolectores")
public class RecolectorController {

    @Autowired
    private UserClient userClient;

    // Tu método original de prueba
    @GetMapping
    public List<String> listarRecolectores() {
        return List.of("Recolector 1 - Prueba", "Recolector 2 - Prueba");
    }

    // Nuevo método para consultar al user-service de Tony vía OpenFeign
    @GetMapping("/usuario/{id}")
    public UserDto obtenerUsuarioDeTony(@PathVariable("id") Long id) {
        return userClient.getUserById(id);
    }
}