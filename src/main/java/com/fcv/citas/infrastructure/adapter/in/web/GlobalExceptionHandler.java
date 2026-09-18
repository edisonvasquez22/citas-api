package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.domain.exception.CredencialesInvalidasException;
import com.fcv.citas.domain.exception.DocumentoYaRegistradoException;
import com.fcv.citas.domain.exception.EmailYaRegistradoException;
import com.fcv.citas.domain.exception.TokenInvalidoException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacion(MethodArgumentNotValidException ex) {
        var detalles = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", detalles));
    }
}
