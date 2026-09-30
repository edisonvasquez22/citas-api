package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTOs de /api/admin/eps/{epsId}/plans y /api/eps/{epsId}/plans (HU-008). */
public final class PlanEpsDtos {

    private PlanEpsDtos() {}

    public record CrearRequest(@NotNull Long regimenId, @NotBlank String codigo, @NotBlank String nombre) {}

    public record EditarRequest(@NotBlank String nombre) {}

    public record CambiarEstadoRequest(@NotNull Boolean activo) {}

    public record Response(Long id, Long epsId, Long regimenId, String codigo, String nombre, boolean activo) {}
}
