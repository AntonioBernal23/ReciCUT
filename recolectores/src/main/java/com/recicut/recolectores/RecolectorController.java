package com.recicut.recolectores;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/recolectores")
public class RecolectorController {

    @GetMapping
    public List<String> listarRecolectores() {
        return List.of("Recolector 1 - Prueba", "Recolector 2 - Prueba");
    }
}