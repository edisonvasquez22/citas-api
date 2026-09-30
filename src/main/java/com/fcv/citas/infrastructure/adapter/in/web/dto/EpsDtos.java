package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTOs de /api/admin/eps y /api/eps (HU-007). */
public final class EpsDtos {

    private EpsDtos() {}

    public record CrearRequest(@NotBlank String codigo, @NotBlank String nombre) {}

    public record EditarRequest(@NotBlank String nombre) {}

    public record CambiarEstadoRequest(@NotNull Boolean activa) {}

    public record Response(Long id, String codigo, String nombre, boolean activa) {}
}
