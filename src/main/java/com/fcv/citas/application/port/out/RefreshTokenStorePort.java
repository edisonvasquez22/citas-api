package com.fcv.citas.application.port.out;

import java.time.Instant;

/**
 * Denylist/registro de refresh tokens vigentes, necesaria para poder revocar
 * (logout) e implementar rotación (ver RenovarSesionUseCase). La implementación
 * actual es en memoria; ver docs/wiki/llm-wiki/wiki/decisiones.md.
 */
public interface RefreshTokenStorePort {

    void registrar(String jti, String usuarioId, Instant expiraEn);

    boolean estaVigente(String jti);

    void revocar(String jti);
}
