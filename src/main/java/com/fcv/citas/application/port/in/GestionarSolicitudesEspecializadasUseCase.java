package com.fcv.citas.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** HU-016 — Bandeja y decisión de solicitudes especializadas (RF-12, RF-18, RN-03, RN-04, RN-09). */
public interface GestionarSolicitudesEspecializadasUseCase {

    List<Resumen> listarSolicitudes(Long sedeId, Long profesionalId, Long especialidadId, LocalDate fecha);

    Resumen aprobar(Long adminUsuarioId, Long citaId);

    Resumen rechazar(Long adminUsuarioId, Long citaId, String motivo);

    record Resumen(Long citaId, Long pacienteUsuarioId, Long profesionalId, Long sedeId, Long especialidadId,
                    String estado, LocalDateTime inicio, LocalDateTime fin, String motivoDecision) {}
}
