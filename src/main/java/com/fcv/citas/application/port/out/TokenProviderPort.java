package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.domain.model.Usuario;
import java.time.Instant;
import java.util.Set;

/** RF-02: emisión/validación de access y refresh JWT con secretos separados. */
public interface TokenProviderPort {

    String generarAccessToken(Usuario usuario);

    RefreshEmitido generarRefreshToken(Usuario usuario);

    /** @throws com.fcv.citas.domain.exception.TokenInvalidoException si el access token no es válido. */
    TokenClaims validarAccessToken(String token);

    /** @throws com.fcv.citas.domain.exception.TokenInvalidoException si el refresh token no es válido. */
    TokenClaims validarRefreshToken(String token);

    record RefreshEmitido(String token, String jti, Instant expiraEn) {}

    record TokenClaims(String usuarioId, String email, Set<RolNombre> roles, String jti, Instant expiraEn) {}
}
