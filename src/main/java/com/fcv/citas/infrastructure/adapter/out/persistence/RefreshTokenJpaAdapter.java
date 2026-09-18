package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador JPA real sobre `refresh_tokens` (ver MODELO_3FN.md sección 3).
 * Reemplaza al denylist en memoria; inactivo en el perfil "test" (ver
 * testsupport.InMemoryRefreshTokenStoreAdapter).
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
        refreshTokenJpaRepository.save(new RefreshTokenJpaEntity(jti, usuarioId, expiraEn));
    }

    @Override
    public boolean estaVigente(String jti) {
        return refreshTokenJpaRepository.findById(jti)
            .map(RefreshTokenJpaEntity::estaVigente)
            .orElse(false);
    }

    @Override
    @Transactional
    public void revocar(String jti) {
        refreshTokenJpaRepository.findById(jti).ifPresent(entidad -> {
            entidad.revocar();
            refreshTokenJpaRepository.save(entidad);
        });
    }
}
