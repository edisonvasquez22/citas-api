package com.fcv.citas.application.port.in;

/** HU-002 — Login: emite access + refresh token. */
public interface IniciarSesionUseCase {

    Resultado iniciarSesion(Command command);

    record Command(String email, String password) {}

    record Resultado(String accessToken, String refreshToken) {}
}
