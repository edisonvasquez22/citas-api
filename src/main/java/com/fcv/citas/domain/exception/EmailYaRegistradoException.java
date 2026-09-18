package com.fcv.citas.domain.exception;

/** RF-01: el email debe ser único. Mapeada a HTTP 409 (ver GlobalExceptionHandler). */
public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException(String email) {
        super("Ya existe una cuenta registrada con el email: " + email);
    }
}
