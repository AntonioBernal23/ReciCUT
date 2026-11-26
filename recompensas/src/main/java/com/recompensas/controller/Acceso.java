package com.recompensas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class Acceso {

    private Acceso() {
    }

    static boolean esAdmin(String role) {
        return "ADMIN".equals(role);
    }

    static void exigirAdmin(String role) {
        if (!esAdmin(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un administrador puede realizar esta acci\u00f3n");
        }
    }

    static void exigirPropietarioOAdmin(String role, Long solicitante, Long usuarioId) {
        if (!esAdmin(role) && (solicitante == null || !solicitante.equals(usuarioId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para consultar datos de otro usuario");
        }
    }

    static void exigirPropietario(Long solicitante, Long usuarioId) {
        if (solicitante == null || !solicitante.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo puedes realizar esta acci\u00f3n con tu propia cuenta");
        }
    }
}