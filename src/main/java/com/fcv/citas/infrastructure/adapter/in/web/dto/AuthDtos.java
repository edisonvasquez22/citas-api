package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs de request/response de /api/auth/** (HU-001, HU-002). Ver HU-024 para el contrato completo. */
public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotBlank String tipoDocumento,
        @NotBlank String numeroDocumento,
        @NotBlank @Email String email,
        @NotBlank String telefono,
        @NotBlank @Size(min = 8, max = 100) String password
    ) {}

    public record RegisterResponse(String usuarioId, String email) {}

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}

    public record TokenResponse(String accessToken, String refreshToken) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record LogoutRequest(@NotBlank String refreshToken) {}
}
