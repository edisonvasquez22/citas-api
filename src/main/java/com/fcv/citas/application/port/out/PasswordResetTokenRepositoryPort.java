package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.TokenRecuperacion;
import java.util.Optional;

/** Puerto de salida para tokens de recuperación de contraseña (HU-003). */
public interface PasswordResetTokenRepositoryPort {

    TokenRecuperacion guardar(TokenRecuperacion token);

    Optional<TokenRecuperacion> buscarPorHash(String tokenHash);
}
