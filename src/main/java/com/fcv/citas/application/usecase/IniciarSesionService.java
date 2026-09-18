package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.IniciarSesionUseCase;
import com.fcv.citas.application.port.out.PasswordHasherPort;
import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import com.fcv.citas.application.port.out.TokenProviderPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.CredencialesInvalidasException;
import com.fcv.citas.domain.model.Usuario;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class IniciarSesionService implements IniciarSesionUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;
    private final RefreshTokenStorePort refreshTokenStore;

    public IniciarSesionService(UsuarioRepositoryPort usuarioRepository, PasswordHasherPort passwordHasher,
                                 TokenProviderPort tokenProvider, RefreshTokenStorePort refreshTokenStore) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    public Resultado iniciarSesion(Command command) {
        String email = command.email() == null ? null : command.email().trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.buscarPorEmail(email)
            .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordHasher.coincide(command.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        String accessToken = tokenProvider.generarAccessToken(usuario);
        TokenProviderPort.RefreshEmitido refresh = tokenProvider.generarRefreshToken(usuario);
        refreshTokenStore.registrar(refresh.jti(), usuario.getId(), refresh.expiraEn());

        return new Resultado(accessToken, refresh.token());
    }
}
