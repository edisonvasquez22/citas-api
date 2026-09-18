package com.fcv.citas.infrastructure.adapter.out.security;

import com.fcv.citas.application.port.out.TokenProviderPort;
import com.fcv.citas.domain.exception.TokenInvalidoException;
import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.domain.model.Usuario;
import com.fcv.citas.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * Adaptador JWT con jjwt 0.13.0 (ver docs/wiki/llm-wiki/wiki/decisiones.md).
 * Access y refresh usan secretos HMAC separados (JWT_ACCESS_SECRET / JWT_REFRESH_SECRET).
 */
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";

    private final JwtProperties properties;
    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtTokenProviderAdapter(JwtProperties properties) {
        this.properties = properties;
        this.accessKey = Keys.hmacShaKeyFor(properties.getAccessSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(properties.getRefreshSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generarAccessToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(Duration.ofMinutes(properties.getAccessMinutes()));
        List<String> roles = usuario.getRoles().stream().map(Enum::name).toList();

        return Jwts.builder()
            .subject(usuario.getId())
            .claim(CLAIM_EMAIL, usuario.getEmail())
            .claim(CLAIM_ROLES, roles)
            .issuedAt(Date.from(ahora))
            .expiration(Date.from(expira))
            .signWith(accessKey)
            .compact();
    }

    @Override
    public RefreshEmitido generarRefreshToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(Duration.ofDays(properties.getRefreshDays()));
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
            .id(jti)
            .subject(usuario.getId())
            .issuedAt(Date.from(ahora))
            .expiration(Date.from(expira))
            .signWith(refreshKey)
            .compact();

        return new RefreshEmitido(token, jti, expira);
    }

    @Override
    public TokenClaims validarAccessToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token).getPayload();
            Set<RolNombre> roles = extraerRoles(claims);
            return new TokenClaims(claims.getSubject(), claims.get(CLAIM_EMAIL, String.class), roles,
                claims.getId(), claims.getExpiration().toInstant());
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidoException("Access token inválido o expirado");
        }
    }

    @Override
    public TokenClaims validarRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(token).getPayload();
            return new TokenClaims(claims.getSubject(), null, Set.of(), claims.getId(),
                claims.getExpiration().toInstant());
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidoException("Refresh token inválido o expirado");
        }
    }

    @SuppressWarnings("unchecked")
    private Set<RolNombre> extraerRoles(Claims claims) {
        Object rawRoles = claims.get(CLAIM_ROLES);
        if (!(rawRoles instanceof List<?> lista)) {
            return Set.of();
        }
        return ((List<Object>) lista).stream()
            .map(Object::toString)
            .map(RolNombre::valueOf)
            .collect(Collectors.toSet());
    }
}
