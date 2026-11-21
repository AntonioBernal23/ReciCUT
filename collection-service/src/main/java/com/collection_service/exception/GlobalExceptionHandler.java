package com.collection_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Traduce excepciones a respuestas HTTP, en UN solo lugar.
 *
 * Gracias a esto el service nunca toca codigos HTTP: lanza
 * CollectionNotFoundException y aqui se decide que eso es un 404.
 * Separacion de capas de verdad.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Forma unica de todos los errores de esta API. */
    public record ApiError(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}

    @ExceptionHandler(CollectionNotFoundException.class)
    public ResponseEntity<ApiError> noEncontrada(CollectionNotFoundException e,
                                                 HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "COLLECTION_NOT_FOUND", e.getMessage(), req);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiError> transicionInvalida(InvalidStatusTransitionException e,
                                                       HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION", e.getMessage(), req);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiError> prohibido(ForbiddenOperationException e,
                                              HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage(), req);
    }

    /** Falla alguna anotacion de validacion del DTO (@NotBlank, @Positive...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException e,
                                               HttpServletRequest req) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", detalle, req);
    }

    /** Falta un header obligatorio, tipicamente X-User-Id. */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> headerFaltante(MissingRequestHeaderException e,
                                                   HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "MISSING_HEADER",
                "Falta el header " + e.getHeaderName(), req);
    }

    /** JSON mal formado o un enum con un valor que no existe. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> jsonInvalido(HttpMessageNotReadableException e,
                                                 HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON",
                "El cuerpo de la peticion no es valido. Revisa el JSON y los valores de los enums.",
                req);
    }

    /** Red de seguridad: cualquier cosa no prevista. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception e, HttpServletRequest req) {
        // El stack trace va al LOG, no al cliente: filtrarlo seria una fuga de informacion.
        log.error("Error no controlado en {}", req.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrio un error inesperado", req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code,
                                           String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiError(
                LocalDateTime.now(), status.value(), code, mensaje, req.getRequestURI()));
    }
}
