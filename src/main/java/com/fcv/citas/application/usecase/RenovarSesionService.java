package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.RenovarSesionUseCase;
import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import com.fcv.citas.application.port.out.TokenProviderPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.TokenInvalidoException;
import com.fcv.citas.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class RenovarSesionService implements RenovarSesionUseCase {

    private final TokenProviderPort tokenProvider;
    private final RefreshTokenStorePort refreshTokenStore;
    private final UsuarioRepositoryPort usuarioRepository;

    public RenovarSesionService(TokenProviderPort tokenProvider, RefreshTokenStorePort refreshTokenStore,
                                 UsuarioRepositoryPort usuarioRepository) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Resultado renovar(Command command) {
        TokenProviderPort.TokenClaims claims = tokenProvider.validarRefreshToken(command.refreshToken());

        if (!refreshTokenStore.estaVigente(claims.jti())) {
            throw new TokenInvalidoException("El refresh token fue revocado o ya expiró");
        }

        Usuario usuario = usuarioRepository.buscarPorId(claims.usuarioId())
            .orElseThrow(() -> new TokenInvalidoException("El usuario del token ya no existe"));

        // Rotación: el refresh usado se revoca de inmediato y se emite uno nuevo.
        refreshTokenStore.revocar(claims.jti());

        String nuevoAccessToken = tokenProvider.generarAccessToken(usuario);
        TokenProviderPort.RefreshEmitido nuevoRefresh = tokenProvider.generarRefreshToken(usuario);
        refreshTokenStore.registrar(nuevoRefresh.jti(), usuario.getId(), nuevoRefresh.expiraEn());

        return new Resultado(nuevoAccessToken, nuevoRefresh.token());
    }
}
