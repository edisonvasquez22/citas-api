package com.fcv.citas.domain.exception;

/**
 * RF-02 / HU-002 CA-02: no debe revelar si falló el email o la contraseña.
 * Mapeada a HTTP 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Email o contraseña incorrectos");
    }
}
