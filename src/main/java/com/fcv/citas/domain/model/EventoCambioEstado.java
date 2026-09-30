package com.fcv.citas.domain.model;

import java.time.LocalDateTime;

public record EventoCambioEstado(
    TipoEventoCita tipo,
    Long citaId,
    Long solicitudReprogramacionId,
    String motivo,
    LocalDateTime momento
) {

    public static EventoCambioEstado deCita(TipoEventoCita tipo, Long citaId, String motivo) {
        return new EventoCambioEstado(tipo, citaId, null, motivo, LocalDateTime.now());
    }

    public static EventoCambioEstado deReprogramacion(TipoEventoCita tipo, Long citaId, Long solicitudId,
                                                      String motivo) {
        return new EventoCambioEstado(tipo, citaId, solicitudId, motivo, LocalDateTime.now());
    }
}
