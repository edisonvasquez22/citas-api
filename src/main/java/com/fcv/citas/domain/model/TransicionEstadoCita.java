package com.fcv.citas.domain.model;

import java.time.LocalDateTime;

/**
 * Agregado de dominio para HU-023 (RF-19, RN-11, RN-12). Un registro de
 * auditoría es inmutable: no existe ninguna operación de edición/borrado
 * (CA-02) — {@code HistorialEstadoCitaPort} solo expone {@code registrar} y
 * {@code listarPorCita}.
 */
public record TransicionEstadoCita(Long id, Long citaId, EstadoCita estado, Long actorUsuarioId,
                                    FuenteCambioEstado fuente, String motivo, LocalDateTime momento) {

    public static TransicionEstadoCita nueva(Long citaId, EstadoCita estado, Long actorUsuarioId,
                                              FuenteCambioEstado fuente, String motivo, LocalDateTime momento) {
        if (citaId == null || estado == null || fuente == null || momento == null) {
            throw new IllegalArgumentException("citaId, estado, fuente y momento son obligatorios");
        }
        return new TransicionEstadoCita(null, citaId, estado, actorUsuarioId, fuente, motivo, momento);
    }
}
