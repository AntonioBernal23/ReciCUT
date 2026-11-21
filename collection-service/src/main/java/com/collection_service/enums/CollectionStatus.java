package com.collection_service.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Ciclo de vida de una solicitud de recoleccion.
 *
 *                      PENDING
 *                    /    |    \
 *            assign /     |      \ reject
 *                  /      | cancel \
 *          ASSIGNED       |          REJECTED
 *            /   \        |
 *     start /     \ cancel|
 *          /       \      |
 *  IN_PROGRESS      CANCELLED
 *       |              ^
 *       | complete     | cancel (solo ADMIN)
 *       v              |
 *   COMPLETED  --------+
 *
 * Las reglas de que transicion es valida viven AQUI y no desperdigadas por el
 * service. Asi hay un solo lugar que cambiar cuando el flujo cambie, y es
 * imposible que dos partes del codigo tengan reglas distintas.
 */
public enum CollectionStatus {

    /** Recien creada. Nadie la ha tomado. */
    PENDING,

    /** Un recolector la acepto, pero todavia no va en camino. */
    ASSIGNED,

    /** El recolector va en camino o esta recogiendo. */
    IN_PROGRESS,

    /** Se recogio y se registraron los pesos reales. Estado final. */
    COMPLETED,

    /** La cancelo el solicitante o un administrador. Estado final. */
    CANCELLED,

    /** Un administrador la rechazo. Estado final. */
    REJECTED;

    /**
     * Mapa de transiciones permitidas.
     *
     * Va en un bloque static y no en el constructor porque Java no deja
     * referenciar otras constantes del enum mientras se estan construyendo.
     * Es un error clasico: el constructor compila pero truena en tiempo de
     * ejecucion con NullPointerException.
     */
    private static final Map<CollectionStatus, Set<CollectionStatus>> TRANSICIONES =
            new EnumMap<>(CollectionStatus.class);

    static {
        TRANSICIONES.put(PENDING,     EnumSet.of(ASSIGNED, CANCELLED, REJECTED));
        TRANSICIONES.put(ASSIGNED,    EnumSet.of(IN_PROGRESS, CANCELLED));
        TRANSICIONES.put(IN_PROGRESS, EnumSet.of(COMPLETED, CANCELLED));
        TRANSICIONES.put(COMPLETED,   EnumSet.noneOf(CollectionStatus.class));
        TRANSICIONES.put(CANCELLED,   EnumSet.noneOf(CollectionStatus.class));
        TRANSICIONES.put(REJECTED,    EnumSet.noneOf(CollectionStatus.class));
    }

    /** Un estado final ya no admite ningun cambio. */
    public boolean esFinal() {
        return TRANSICIONES.get(this).isEmpty();
    }

    /** ¿Puedo pasar de este estado al destino? */
    public boolean puedeCambiarA(CollectionStatus destino) {
        return destino != null && TRANSICIONES.get(this).contains(destino);
    }

    /** Los estados a los que se puede ir desde aqui (para mensajes de error). */
    public Set<CollectionStatus> siguientesPosibles() {
        return Collections.unmodifiableSet(TRANSICIONES.get(this));
    }
}
