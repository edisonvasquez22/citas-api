package com.fcv.citas.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** HU-014 — Solicitar cita general con aprobación automática (RF-11, RN-01, RN-02, RN-06). */
public interface SolicitarCitaGeneralUseCase {

    Resultado solicitar(Command command);

    record Command(Long pacienteUsuarioId, Long profesionalId, Long sedeId, Long especialidadId, String motivo,
                    LocalDate fecha, LocalTime horaInicio) {}

    record Resultado(Long citaId, String estado, LocalDateTime inicio, LocalDateTime fin) {}
}
