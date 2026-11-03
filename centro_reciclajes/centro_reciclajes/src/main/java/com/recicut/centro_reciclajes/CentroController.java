package com.recicut.centro_reciclajes;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/centros")
public class CentroController {

    @GetMapping
    public List<String> listarCentros() {
        return List.of("Centro de Reciclaje Norte - Prueba", "Centro de Reciclaje Sur - Prueba");
    }
}