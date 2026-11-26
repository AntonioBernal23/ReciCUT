package com.report_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ApiError(LocalDateTime timestamp, int status,
                           String error, String message, String path) {}

    /** Tipico: ?from=2026-13-45 o una granularidad que no existe. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tipoInvalido(MethodArgumentTypeMismatchException e,
                                                 HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "El parametro '" + e.getName() + "' tiene un valor invalido", req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> argumentoInvalido(IllegalArgumentException e,
                                                      HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", e.getMessage(), req);
    }

    /**
     * Una URL que no existe. Sin esto cae en el catch-all de Exception y la API
     * responde 500: un error del servidor por una peticion mal escrita del
     * cliente. 404 es la respuesta correcta y no ensucia los logs de errores.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> noExiste(NoResourceFoundException e,
                                             HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "La ruta solicitada no existe", req);
    }

    /** La ruta existe pero no con ese verbo (por ejemplo GET donde va PATCH). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> metodoNoPermitido(HttpRequestMethodNotSupportedException e,
                                                      HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "El metodo " + e.getMethod() + " no esta permitido en esta ruta", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception e, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrio un error inesperado", req);
    }

    private ResponseEntity<ApiError> build(HttpStatus s, String code,
                                           String msg, HttpServletRequest req) {
        return ResponseEntity.status(s).body(
                new ApiError(LocalDateTime.now(), s.value(), code, msg, req.getRequestURI()));
    }
}
