package com.collection_service.exception;

/** La solicitud no existe. -> 404 */
public class CollectionNotFoundException extends RuntimeException {
    public CollectionNotFoundException(Long id) {
        super("No existe la solicitud de recoleccion " + id);
    }
}
