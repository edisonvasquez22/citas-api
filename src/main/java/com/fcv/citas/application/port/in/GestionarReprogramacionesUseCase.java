package com.fcv.citas.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** HU-020: ADMIN gestiona la bandeja de solicitudes de reprogramación PENDING. */
public interface GestionarReprogramacionesUseCase {

    /** RF-18: bandeja PENDING con filtros opcionales; {@code sedeId} y {@code fecha} aplican a la sede/fecha SOLICITADAS. */
    List<Resumen> listarPendientes(Long sedeId, Long profesionalId, Long especialidadId, LocalDate fecha);

    Resumen aprobar(Long adminUsuarioId, Long solicitudId);

    Resumen rechazar(Long adminUsuarioId, Long solicitudId, String motivo);

    record Resumen(Long solicitudId, Long citaId, Long profesionalId, Long sedeSolicitadaId, String estado,
                    LocalDateTime inicioAnterior, LocalDateTime finAnterior, LocalDateTime inicioSolicitado,
                    LocalDateTime finSolicitado, String motivoDecision) {}
}
