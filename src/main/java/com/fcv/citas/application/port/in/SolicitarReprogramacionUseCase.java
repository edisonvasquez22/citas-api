package com.fcv.citas.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** HU-019: el USER autenticado solicita reprogramar una cita propia APPROVED y futura. */
public interface SolicitarReprogramacionUseCase {

    Resultado solicitar(Command command);

    record Command(Long pacienteUsuarioId, Long citaId, Long nuevaSedeId, LocalDate nuevaFecha,
                    LocalTime nuevaHoraInicio) {}

    record Resultado(Long solicitudId, Long citaId, String estado, LocalDateTime inicioSolicitado,
                      LocalDateTime finSolicitado) {}
}
