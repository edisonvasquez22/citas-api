package com.fcv.citas.domain.exception;

/**
 * Access o refresh token inválido, expirado o revocado (HU-002 CA-04).
 * Mapeada a HTTP 401; también es capturada en silencio por el filtro JWT
 * para peticiones autenticadas (ver JwtAuthenticationFilter).
 */
public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
