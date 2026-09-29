package com.fcv.citas.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

/** HU-020: ADMIN gestiona la bandeja de solicitudes de reprogramación PENDING. */
public interface GestionarReprogramacionesUseCase {

    List<Resumen> listarPendientes();

    Resumen aprobar(Long adminUsuarioId, Long solicitudId);

    Resumen rechazar(Long adminUsuarioId, Long solicitudId, String motivo);

    record Resumen(Long solicitudId, Long citaId, Long profesionalId, Long sedeSolicitadaId, String estado,
                    LocalDateTime inicioAnterior, LocalDateTime finAnterior, LocalDateTime inicioSolicitado,
                    LocalDateTime finSolicitado, String motivoDecision) {}
}
