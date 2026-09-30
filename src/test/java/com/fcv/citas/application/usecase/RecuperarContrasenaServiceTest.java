package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.out.PasswordHasherPort;
import com.fcv.citas.domain.exception.TokenInvalidoException;
import com.fcv.citas.domain.model.TokenRecuperacion;
import com.fcv.citas.domain.model.Usuario;
import com.fcv.citas.testsupport.InMemoryPasswordResetTokenRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryUsuarioRepositoryAdapter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-003: CA-01 a CA-03. */
class RecuperarContrasenaServiceTest {

    private static final String TOKEN_CONOCIDO = "token-de-prueba-conocido";

    private InMemoryUsuarioRepositoryAdapter usuarioRepository;
    private InMemoryPasswordResetTokenRepositoryAdapter tokenRepository;
    private RecuperarContrasenaService service;
    private Long usuarioIdNumerico;
    private String usuarioId;

    @BeforeEach
    void setUp() {
        usuarioRepository = new InMemoryUsuarioRepositoryAdapter();
        tokenRepository = new InMemoryPasswordResetTokenRepositoryAdapter();
        service = new RecuperarContrasenaService(usuarioRepository, tokenRepository, new FakePasswordHasher());
        Usuario guardado = usuarioRepository.guardar(Usuario.registrarNuevo("Ana", "Pérez", "CC", "123456789",
            "ana@example.com", "3001234567", "hash-original"));
        usuarioId = guardado.getId();
        usuarioIdNumerico = Long.valueOf(usuarioId);
    }

    @Test
    void solicitar_conEmailExistente_generaUnTokenParaEseUsuario() {
        service.solicitar("ana@example.com");

        assertThat(tokenRepository.todos()).hasSize(1);
        assertThat(tokenRepository.todos().iterator().next().getUsuarioId()).isEqualTo(usuarioIdNumerico);
    }

    @Test
    void solicitar_conEmailInexistente_noGeneraNingunToken() {
        service.solicitar("no-existe@example.com");

        assertThat(tokenRepository.todos()).isEmpty();
    }

    @Test
    void confirmar_conTokenValido_actualizaLaPasswordYConsumeElToken() {
        sembrarTokenVigente();

        service.confirmar(TOKEN_CONOCIDO, "nueva-clave-segura");

        Usuario actualizado = usuarioRepository.buscarPorId(usuarioId).orElseThrow();
        assertThat(actualizado.getPasswordHash()).isEqualTo("fake-hash:nueva-clave-segura");
        // CA-03: el mismo token ya no sirve una segunda vez (de un solo uso).
        assertThatThrownBy(() -> service.confirmar(TOKEN_CONOCIDO, "otra-clave"))
            .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void confirmar_conTokenExpirado_seRechazaSinModificarLaPassword() {
        sembrarToken(Instant.now().minusSeconds(60), null);

        assertThatThrownBy(() -> service.confirmar(TOKEN_CONOCIDO, "otra-clave"))
            .isInstanceOf(TokenInvalidoException.class);
        assertThat(usuarioRepository.buscarPorId(usuarioId).orElseThrow().getPasswordHash())
            .isEqualTo("hash-original");
    }

    @Test
    void confirmar_conTokenYaUsado_seRechazaSinModificarLaPassword() {
        sembrarToken(Instant.now().plusSeconds(1800), Instant.now().minusSeconds(60));

        assertThatThrownBy(() -> service.confirmar(TOKEN_CONOCIDO, "otra-clave"))
            .isInstanceOf(TokenInvalidoException.class);
        assertThat(usuarioRepository.buscarPorId(usuarioId).orElseThrow().getPasswordHash())
            .isEqualTo("hash-original");
    }

    @Test
    void confirmar_conTokenInexistente_seRechaza() {
        assertThatThrownBy(() -> service.confirmar("token-que-nunca-existio", "otra-clave"))
            .isInstanceOf(TokenInvalidoException.class);
    }

    private void sembrarTokenVigente() {
        sembrarToken(Instant.now().plusSeconds(1800), null);
    }

    private void sembrarToken(Instant expiraEn, Instant usadoEn) {
        TokenRecuperacion tokenEntidad = TokenRecuperacion.reconstruir(null, usuarioIdNumerico,
            sha256(TOKEN_CONOCIDO), expiraEn, usadoEn);
        tokenRepository.guardar(tokenEntidad);
    }

    private static String sha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class FakePasswordHasher implements PasswordHasherPort {
        @Override
        public String hash(String rawPassword) {
            return "fake-hash:" + rawPassword;
        }

        @Override
        public boolean coincide(String rawPassword, String hash) {
            return ("fake-hash:" + rawPassword).equals(hash);
        }
    }
}
