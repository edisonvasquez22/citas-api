package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.CerrarSesionUseCase;
import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import com.fcv.citas.application.port.out.TokenProviderPort;
import com.fcv.citas.domain.exception.TokenInvalidoException;
import org.springframework.stereotype.Service;

@Service
public class CerrarSesionService implements CerrarSesionUseCase {

    private final TokenProviderPort tokenProvider;
    private final RefreshTokenStorePort refreshTokenStore;

    public CerrarSesionService(TokenProviderPort tokenProvider, RefreshTokenStorePort refreshTokenStore) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    public void cerrarSesion(Command command) {
        try {
            TokenProviderPort.TokenClaims claims = tokenProvider.validarRefreshToken(command.refreshToken());
            refreshTokenStore.revocar(claims.jti());
        } catch (TokenInvalidoException ignored) {
            // Logout es idempotente: un token ya inválido/expirado equivale a una sesión ya cerrada.
        }
    }
}
