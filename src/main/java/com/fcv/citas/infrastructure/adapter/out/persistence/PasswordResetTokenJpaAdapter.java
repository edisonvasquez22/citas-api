package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.PasswordResetTokenRepositoryPort;
import com.fcv.citas.domain.model.TokenRecuperacion;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `password_reset_tokens` (HU-003). Inactivo en el perfil "test". */
@Component
@Profile("!test")
public class PasswordResetTokenJpaAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;

    public PasswordResetTokenJpaAdapter(PasswordResetTokenJpaRepository passwordResetTokenJpaRepository) {
        this.passwordResetTokenJpaRepository = passwordResetTokenJpaRepository;
    }

    @Override
    @Transactional
    public TokenRecuperacion guardar(TokenRecuperacion token) {
        PasswordResetTokenJpaEntity entidad = new PasswordResetTokenJpaEntity(token.getId(), token.getUsuarioId(),
            token.getTokenHash(), token.getExpiraEn(), token.getUsadoEn());
        return aDominio(passwordResetTokenJpaRepository.save(entidad));
    }

    @Override
    public Optional<TokenRecuperacion> buscarPorHash(String tokenHash) {
        return passwordResetTokenJpaRepository.findByTokenHash(tokenHash).map(this::aDominio);
    }

    private TokenRecuperacion aDominio(PasswordResetTokenJpaEntity entidad) {
        return TokenRecuperacion.reconstruir(entidad.getId(), entidad.getUserId(), entidad.getTokenHash(),
            entidad.getExpiresAt(), entidad.getUsedAt());
    }
}
