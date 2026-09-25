package com.fcv.citas.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** HU-015 — Solicitar cita especializada (RF-12, RN-01, RN-03, RN-08). */
public interface SolicitarCitaEspecializadaUseCase {

    Resultado solicitar(Command command);

    record Command(Long pacienteUsuarioId, Long profesionalId, Long sedeId, Long especialidadId, String motivo,
                    LocalDate fecha, LocalTime horaInicio) {}

    record Resultado(Long citaId, String estado, LocalDateTime inicio, LocalDateTime fin) {}
}
