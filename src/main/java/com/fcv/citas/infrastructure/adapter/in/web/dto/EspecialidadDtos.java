package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTOs de /api/admin/specialties y /api/specialties (HU-009). */
public final class EspecialidadDtos {

    private EspecialidadDtos() {}

    public record CrearRequest(
        @NotBlank String codigo,
        @NotBlank String nombre,
        @NotNull Integer duracionMinutos,
        boolean general,
        boolean requiereAprobacionAdmin
    ) {}

    public record EditarRequest(
        @NotBlank String nombre,
        @NotNull Integer duracionMinutos,
        boolean general,
        boolean requiereAprobacionAdmin
    ) {}

    public record CambiarEstadoRequest(@NotNull Boolean activa) {}

    public record Response(Long id, String codigo, String nombre, int duracionMinutos, boolean general,
                            boolean requiereAprobacionAdmin, boolean activa) {}
}
