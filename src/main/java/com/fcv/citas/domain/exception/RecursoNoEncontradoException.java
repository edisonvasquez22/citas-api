package com.fcv.citas.domain.exception;

/** Recurso de dominio (especialidad, profesional, bloque, cita) inexistente. Mapeada a HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
