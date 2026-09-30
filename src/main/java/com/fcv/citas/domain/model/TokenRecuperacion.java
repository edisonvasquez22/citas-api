package com.fcv.citas.domain.model;

import com.fcv.citas.domain.exception.TokenInvalidoException;
import java.time.Instant;

/**
 * Agregado de dominio para HU-003 (RF-03). Token temporal de un solo uso para
 * recuperar contraseña, análogo en espíritu a `refresh_tokens` (ver
 * RefreshTokenJpaAdapter): la fila se identifica por el hash SHA-256 del
 * token en claro, nunca por el token mismo. Sin dependencias de Spring/JPA.
 */
public final class TokenRecuperacion {

    private final Long id;
    private final Long usuarioId;
    private final String tokenHash;
    private final Instant expiraEn;
    private final Instant usadoEn;

    private TokenRecuperacion(Long id, Long usuarioId, String tokenHash, Instant expiraEn, Instant usadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.expiraEn = expiraEn;
        this.usadoEn = usadoEn;
    }

    public static TokenRecuperacion crear(Long usuarioId, String tokenHash, Instant expiraEn) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash no puede estar vacío");
        }
        if (expiraEn == null) {
            throw new IllegalArgumentException("expiraEn no puede ser nulo");
        }
        return new TokenRecuperacion(null, usuarioId, tokenHash, expiraEn, null);
    }

    public static TokenRecuperacion reconstruir(Long id, Long usuarioId, String tokenHash, Instant expiraEn,
                                                 Instant usadoEn) {
        return new TokenRecuperacion(id, usuarioId, tokenHash, expiraEn, usadoEn);
    }

    /** CA-01: temporal. CA-03: de un solo uso (rechaza si ya está usado o expiró). */
    public boolean estaVigente() {
        return usadoEn == null && expiraEn.isAfter(Instant.now());
    }

    /** CA-02: consumir un token no vigente (expirado o ya usado) se rechaza sin modificar nada más. */
    public TokenRecuperacion consumir() {
        if (!estaVigente()) {
            throw new TokenInvalidoException("Token de recuperación inválido, expirado o ya utilizado");
        }
        return new TokenRecuperacion(id, usuarioId, tokenHash, expiraEn, Instant.now());
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public Instant getUsadoEn() {
        return usadoEn;
    }
}
