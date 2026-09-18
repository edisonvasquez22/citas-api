package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fcv.citas.application.port.in.IniciarSesionUseCase.Command;
import com.fcv.citas.application.port.in.IniciarSesionUseCase.Resultado;
import com.fcv.citas.application.port.out.PasswordHasherPort;
import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import com.fcv.citas.application.port.out.TokenProviderPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.CredencialesInvalidasException;
import com.fcv.citas.domain.model.Usuario;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IniciarSesionServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordHasherPort passwordHasher;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private RefreshTokenStorePort refreshTokenStore;

    private IniciarSesionService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new IniciarSesionService(usuarioRepository, passwordHasher, tokenProvider, refreshTokenStore);
        usuario = Usuario.registrarNuevo("Ana", "Pérez", "CC", "1000000001", "ana@example.com", "3000000000", "hash-guardado");
    }

    @Test
    void iniciarSesion_conCredencialesValidas_emiteAccessYRefreshToken() {
        when(usuarioRepository.buscarPorEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        when(passwordHasher.coincide("clave-correcta", "hash-guardado")).thenReturn(true);
        when(tokenProvider.generarAccessToken(usuario)).thenReturn("access-token");
        TokenProviderPort.RefreshEmitido refreshEmitido =
            new TokenProviderPort.RefreshEmitido("refresh-token", "jti-1", Instant.now().plusSeconds(3600));
        when(tokenProvider.generarRefreshToken(usuario)).thenReturn(refreshEmitido);

        Resultado resultado = service.iniciarSesion(new Command("ana@example.com", "clave-correcta"));

        assertThat(resultado.accessToken()).isEqualTo("access-token");
        assertThat(resultado.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void iniciarSesion_conEmailInexistente_lanzaCredencialesInvalidas() {
        when(usuarioRepository.buscarPorEmail("desconocido@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciarSesion(new Command("desconocido@example.com", "cualquiera")))
            .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void iniciarSesion_conPasswordIncorrecto_lanzaCredencialesInvalidas() {
        when(usuarioRepository.buscarPorEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        when(passwordHasher.coincide("clave-incorrecta", "hash-guardado")).thenReturn(false);

        assertThatThrownBy(() -> service.iniciarSesion(new Command("ana@example.com", "clave-incorrecta")))
            .isInstanceOf(CredencialesInvalidasException.class);
    }
}
