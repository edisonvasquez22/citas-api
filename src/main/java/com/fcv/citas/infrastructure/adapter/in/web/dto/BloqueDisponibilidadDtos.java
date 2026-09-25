package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/** DTOs de /api/professionals/me/availability-blocks (HU-012). */
public final class BloqueDisponibilidadDtos {

    private BloqueDisponibilidadDtos() {}

    public record CrearRequest(@NotNull Long sedeId, @NotNull LocalDate fecha, @NotNull LocalTime horaInicio,
                                @NotNull LocalTime horaFin) {}

    public record EditarRequest(@NotNull Long sedeId, @NotNull LocalDate fecha, @NotNull LocalTime horaInicio,
                                 @NotNull LocalTime horaFin) {}

    public record Response(Long id, Long profesionalId, Long sedeId, LocalDate fecha, LocalTime horaInicio,
                            LocalTime horaFin, boolean activo) {}
}
