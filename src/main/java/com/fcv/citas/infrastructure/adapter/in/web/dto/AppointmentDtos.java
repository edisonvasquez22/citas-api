package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** DTOs de /api/appointments/** (HU-014, HU-015). */
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

    public record Response(Long citaId, String estado, LocalDateTime inicio, LocalDateTime fin) {}
}
