package com.collection_service.exception;

import com.collection_service.enums.CollectionStatus;

/** Se intento un cambio de estado que la maquina de estados no permite. -> 409 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(Long id, CollectionStatus actual,
                                            CollectionStatus intentado) {
        super("La solicitud %d esta en %s y no puede pasar a %s. Transiciones validas: %s"
                .formatted(id, actual, intentado, actual.siguientesPosibles()));
    }

    public InvalidStatusTransitionException(String mensaje) {
        super(mensaje);
    }
}
