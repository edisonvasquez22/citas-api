package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fcv.citas.application.port.in.RenovarSesionUseCase.Command;
import com.fcv.citas.application.port.in.RenovarSesionUseCase.Resultado;
import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import com.fcv.citas.application.port.out.TokenProviderPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.TokenInvalidoException;
import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.domain.model.Usuario;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RenovarSesionServiceTest {

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private RefreshTokenStorePort refreshTokenStore;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private RenovarSesionService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new RenovarSesionService(tokenProvider, refreshTokenStore, usuarioRepository);
        // reconstruir (no registrarNuevo): representa un usuario ya persistido que se busca por id.
        usuario = Usuario.reconstruir("1", "Ana", "Pérez", "CC", "1000000001", "ana@example.com", "3000000000",
            "hash", Set.of(RolNombre.USER), true);
    }

    @Test
    void renovar_conRefreshVigente_rotaElTokenYEmiteUnoNuevo() {
        TokenProviderPort.TokenClaims claims =
            new TokenProviderPort.TokenClaims(usuario.getId(), null, Set.of(), "jti-viejo", Instant.now().plusSeconds(60));
        when(tokenProvider.validarRefreshToken("refresh-viejo")).thenReturn(claims);
        when(refreshTokenStore.estaVigente("jti-viejo")).thenReturn(true);
        when(usuarioRepository.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
        when(tokenProvider.generarAccessToken(usuario)).thenReturn("nuevo-access");
        TokenProviderPort.RefreshEmitido nuevoRefresh =
            new TokenProviderPort.RefreshEmitido("nuevo-refresh", "jti-nuevo", Instant.now().plusSeconds(3600));
        when(tokenProvider.generarRefreshToken(usuario)).thenReturn(nuevoRefresh);

        Resultado resultado = service.renovar(new Command("refresh-viejo"));

        assertThat(resultado.accessToken()).isEqualTo("nuevo-access");
        assertThat(resultado.refreshToken()).isEqualTo("nuevo-refresh");
    }

    @Test
    void renovar_conRefreshRevocado_lanzaTokenInvalido() {
        TokenProviderPort.TokenClaims claims =
            new TokenProviderPort.TokenClaims(usuario.getId(), null, Set.of(), "jti-revocado", Instant.now().plusSeconds(60));
        when(tokenProvider.validarRefreshToken("refresh-revocado")).thenReturn(claims);
        when(refreshTokenStore.estaVigente("jti-revocado")).thenReturn(false);

        assertThatThrownBy(() -> service.renovar(new Command("refresh-revocado")))
            .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void renovar_conTokenMalformado_propagaTokenInvalidoDesdeElProvider() {
        when(tokenProvider.validarRefreshToken("basura")).thenThrow(new TokenInvalidoException("Refresh token inválido o expirado"));

        assertThatThrownBy(() -> service.renovar(new Command("basura")))
            .isInstanceOf(TokenInvalidoException.class);
    }
}
