package com.report_service.events;

/**
 * Copia del evento que publica auth-service.
 *
 * El mismo evento, tres consumidores distintos (user-service para el perfil,
 * report-service para el ranking, y manana quiza notificaciones). auth-service
 * no sabe ni le importa quien lo escucha: eso es desacoplamiento.
 */
public record UserRegisteredEvent(
        Long userId,
        String name,
        String lastName,
        String picture
) {}
