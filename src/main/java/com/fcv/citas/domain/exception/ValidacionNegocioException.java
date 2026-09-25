package com.fcv.citas.domain.exception;

/**
 * Regla de negocio del PRD violada (duración inválida, especialidad primaria
 * duplicada, sede no habilitada, bloque solapado/en el pasado/comprometido,
 * especialidad no asociada al profesional, motivo de rechazo faltante, etc.).
 * Mapeada a HTTP 400 (ver GlobalExceptionHandler).
 */
public class ValidacionNegocioException extends RuntimeException {

    public ValidacionNegocioException(String mensaje) {
        super(mensaje);
    }
}
