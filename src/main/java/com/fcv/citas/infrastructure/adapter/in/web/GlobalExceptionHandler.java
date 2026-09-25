package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.domain.exception.CredencialesInvalidasException;
import com.fcv.citas.domain.exception.DocumentoYaRegistradoException;
import com.fcv.citas.domain.exception.EmailYaRegistradoException;
import com.fcv.citas.domain.exception.HorarioNoDisponibleException;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.TokenInvalidoException;
import com.fcv.citas.domain.exception.TransicionEstadoInvalidaException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** No exponer stack traces ni detalles internos (RESTRICCIONES_TECNICAS.md: sin logging de secretos). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({EmailYaRegistradoException.class, DocumentoYaRegistradoException.class})
    public ResponseEntity<ApiError> handleConflicto(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler({CredencialesInvalidasException.class, TokenInvalidoException.class})
    public ResponseEntity<ApiError> handleNoAutorizado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<ApiError> handleValidacionNegocio(ValidacionNegocioException ex) {
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler({HorarioNoDisponibleException.class, TransicionEstadoInvalidaException.class})
    public ResponseEntity<ApiError> handleConflictoDeEstado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacion(MethodArgumentNotValidException ex) {
        var detalles = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", detalles));
    }
}
