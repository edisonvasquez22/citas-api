package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

/** DTOs de /api/admin/professionals (HU-010, HU-011). */
public final class ProfesionalDtos {

    private ProfesionalDtos() {}

    public record EspecialidadAsignadaRequest(@NotNull Long especialidadId, boolean primaria) {}

    public record RegistrarRequest(
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotBlank String tipoDocumento,
        @NotBlank String numeroDocumento,
        @NotBlank @Email String email,
        @NotBlank String telefono,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String codigoProfesional,
        @NotBlank String matricula,
        @NotEmpty List<@Valid EspecialidadAsignadaRequest> especialidades,
        @NotEmpty Set<Long> sedeIds
    ) {}

    public record CambiarEstadoRequest(@NotNull Boolean activo) {}

    public record Response(Long profesionalId, Long usuarioId, String codigoProfesional, boolean activo) {}
}
