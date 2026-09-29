package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** DTOs de /api/appointments/** (HU-014, HU-015, HU-019). */
public final class AppointmentDtos {

    private AppointmentDtos() {}

    public record SolicitarRequest(
        @NotNull Long profesionalId,
        @NotNull Long sedeId,
        @NotNull Long especialidadId,
        String motivo,
        @NotNull LocalDate fecha,
        @NotNull LocalTime horaInicio
    ) {}

    /** HU-019: POST /api/appointments/{id}/reschedule. */
    public record ReprogramarRequest(@NotNull Long sedeId, @NotNull LocalDate fecha, @NotNull LocalTime horaInicio) {}

    public record ReprogramarResponse(Long solicitudId, Long citaId, String estado, LocalDateTime inicioSolicitado,
                                       LocalDateTime finSolicitado) {}

    public record Response(Long citaId, String estado, LocalDateTime inicio, LocalDateTime fin) {}

    /** HU-017: GET /api/appointments/mine. reprogramacion es la última solicitud conocida para esta cita, o null. */
    public record MiCitaResponse(Long citaId, Long sedeId, Long profesionalId, Long especialidadId, String estado,
                                  LocalDateTime inicio, LocalDateTime fin, String motivoDecision,
                                  ReprogramacionInfo reprogramacion) {}

    /** HU-019/HU-020: desenlace de la última solicitud de reprogramación de una cita. */
    public record ReprogramacionInfo(Long solicitudId, String estado, LocalDateTime inicioSolicitado,
                                      LocalDateTime finSolicitado, String motivoDecision) {}
}
