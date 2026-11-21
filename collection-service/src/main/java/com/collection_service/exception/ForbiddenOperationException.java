package com.collection_service.exception;

/** Esta autenticado, pero la solicitud no es suya o no tiene el rol. -> 403 */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String mensaje) {
        super(mensaje);
    }
}
