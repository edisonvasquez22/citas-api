package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTOs de /api/users/me y /api/users/me/afiliacion (HU-004, HU-005). */
public final class UserDtos {

    private UserDtos() {}

    public record PerfilResponse(String id, String nombres, String apellidos, String tipoDocumento,
                                  String numeroDocumento, String email, String telefono) {}

    public record ActualizarPerfilRequest(@NotBlank String nombres, @NotBlank String apellidos,
                                           @NotBlank String telefono) {}

    public record AsociarAfiliacionRequest(@NotNull Long epsId, @NotNull Long planId,
                                            @NotBlank String numeroAfiliacion) {}

    public record AfiliacionResponse(Long afiliacionId, Long epsId, String epsNombre, Long planId, String planNombre,
                                      Long regimenId, String numeroAfiliacion) {}
}
