package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador JPA real sobre `refresh_tokens` (ver MODELO_3FN.md sección 3).
 * Reemplaza al denylist en memoria; inactivo en el perfil "test" (ver
 * testsupport.InMemoryRefreshTokenStoreAdapter).
 *
 * El esquema (copia exacta de database/reference/db.sql) identifica cada fila
 * por `token_hash`, no por el `jti` en claro: aquí se guarda/busca por el
 * SHA-256 hexadecimal del `jti`, para que una fuga de la tabla no exponga el
 * identificador que viaja firmado dentro del JWT.
 */
@Component
@Profile("!test")
public class RefreshTokenJpaAdapter implements RefreshTokenStorePort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    public RefreshTokenJpaAdapter(RefreshTokenJpaRepository refreshTokenJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
    }

    @Override
    @Transactional
    public void registrar(String jti, String usuarioId, Instant expiraEn) {
        refreshTokenJpaRepository.save(
            new RefreshTokenJpaEntity(Long.valueOf(usuarioId), hash(jti), expiraEn));
    }

    @Override
    public boolean estaVigente(String jti) {
        return refreshTokenJpaRepository.findByTokenHash(hash(jti))
            .map(RefreshTokenJpaEntity::estaVigente)
            .orElse(false);
    }

    @Override
    @Transactional
    public void revocar(String jti) {
        refreshTokenJpaRepository.findByTokenHash(hash(jti)).ifPresent(entidad -> {
            entidad.revocar();
            refreshTokenJpaRepository.save(entidad);
        });
    }

    private static String hash(String jti) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(jti.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
