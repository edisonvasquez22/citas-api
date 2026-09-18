package com.fcv.citas.application.port.in;

/**
 * HU-002 CA-03/CA-04 — Renueva la sesión a partir de un refresh token vigente.
 * Estrategia elegida: rotación (el refresh usado queda revocado y se emite uno nuevo).
 */
public interface RenovarSesionUseCase {

    Resultado renovar(Command command);

    record Command(String refreshToken) {}

    record Resultado(String accessToken, String refreshToken) {}
}
