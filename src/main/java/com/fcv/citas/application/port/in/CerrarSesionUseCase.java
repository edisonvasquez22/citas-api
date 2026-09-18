package com.fcv.citas.application.port.in;

/** HU-002 CA-05 — Logout: revoca el refresh token de la sesión actual. Idempotente. */
public interface CerrarSesionUseCase {

    void cerrarSesion(Command command);

    record Command(String refreshToken) {}
}
