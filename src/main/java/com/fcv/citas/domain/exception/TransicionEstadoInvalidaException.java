package com.fcv.citas.domain.exception;

/**
 * RN-11: se intentó una transición de estado no permitida (p. ej. decidir una
 * cita que ya no está en REQUESTED). Mapeada a HTTP 409.
 */
public class TransicionEstadoInvalidaException extends RuntimeException {

    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
