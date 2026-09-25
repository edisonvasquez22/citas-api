package com.fcv.citas.domain.exception;

/**
 * RN-01: el horario solicitado ya no está libre al momento de confirmar (lo
 * tomó otro usuario, incluida la pérdida de una carrera de concurrencia).
 * Mapeada a HTTP 409 (ver GlobalExceptionHandler).
 */
public class HorarioNoDisponibleException extends RuntimeException {

    public HorarioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
