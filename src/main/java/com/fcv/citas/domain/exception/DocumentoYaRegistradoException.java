package com.fcv.citas.domain.exception;

/** RF-01: el número de documento debe ser único. Mapeada a HTTP 409. */
public class DocumentoYaRegistradoException extends RuntimeException {

    public DocumentoYaRegistradoException(String numeroDocumento) {
        super("Ya existe una cuenta registrada con el documento: " + numeroDocumento);
    }
}
