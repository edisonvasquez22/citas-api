package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.RecuperarContrasenaUseCase;
import com.fcv.citas.application.port.out.PasswordHasherPort;
import com.fcv.citas.application.port.out.PasswordResetTokenRepositoryPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.TokenInvalidoException;
import com.fcv.citas.domain.model.TokenRecuperacion;
import com.fcv.citas.domain.model.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * HU-003 (RF-03): sin SMTP obligatorio. El token en claro nunca viaja en la
 * respuesta HTTP (eso filtraría si el email existe, violando CA-01); en
 * desarrollo se expone solo por log estructurado del servidor (ver
 * HU-003 "Contexto y descripción"). Solo se guarda su hash SHA-256, igual
 * que `refresh_tokens` (ver RefreshTokenJpaAdapter).
 */
@Service
public class RecuperarContrasenaService implements RecuperarContrasenaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecuperarContrasenaService.class);
    private static final Duration VIGENCIA = Duration.ofMinutes(30);

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordResetTokenRepositoryPort tokenRepository;
    private final PasswordHasherPort passwordHasher;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecuperarContrasenaService(UsuarioRepositoryPort usuarioRepository,
                                       PasswordResetTokenRepositoryPort tokenRepository,
                                       PasswordHasherPort passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void solicitar(String email) {
        usuarioRepository.buscarPorEmail(email).ifPresent(usuario -> {
            String tokenPlano = generarTokenPlano();
            Instant expiraEn = Instant.now().plus(VIGENCIA);
            tokenRepository.guardar(
                TokenRecuperacion.crear(Long.valueOf(usuario.getId()), hash(tokenPlano), expiraEn));
            log.info("Token de recuperación de contraseña generado para usuarioId={}, expira={}: {}",
                usuario.getId(), expiraEn, tokenPlano);
        });
        // CA-01: no hay nada más que hacer aquí a propósito — el controller responde
        // siempre el mismo mensaje genérico, exista o no el email.
    }

    @Override
    public void confirmar(String tokenPlano, String nuevaPassword) {
        TokenRecuperacion tokenEntidad = tokenRepository.buscarPorHash(hash(tokenPlano))
            .orElseThrow(() -> new TokenInvalidoException("Token de recuperación inválido, expirado o ya utilizado"));
        TokenRecuperacion consumido = tokenEntidad.consumir();
        tokenRepository.guardar(consumido);

        Usuario usuario = usuarioRepository.buscarPorId(String.valueOf(tokenEntidad.getUsuarioId()))
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "Usuario no encontrado: " + tokenEntidad.getUsuarioId()));
        usuarioRepository.guardar(usuario.cambiarPassword(passwordHasher.hash(nuevaPassword)));
    }

    private String generarTokenPlano() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String tokenPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(tokenPlano.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
