package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.CerrarSesionUseCase;
import com.fcv.citas.application.port.in.IniciarSesionUseCase;
import com.fcv.citas.application.port.in.RecuperarContrasenaUseCase;
import com.fcv.citas.application.port.in.RegistrarUsuarioUseCase;
import com.fcv.citas.application.port.in.RenovarSesionUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HU-001 (registro), HU-002 (login/refresh/logout) y HU-003 (recuperar contraseña). Ver HU-024 para el contrato completo. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final IniciarSesionUseCase iniciarSesionUseCase;
    private final RenovarSesionUseCase renovarSesionUseCase;
    private final CerrarSesionUseCase cerrarSesionUseCase;
    private final RecuperarContrasenaUseCase recuperarContrasenaUseCase;

    public AuthController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                           IniciarSesionUseCase iniciarSesionUseCase,
                           RenovarSesionUseCase renovarSesionUseCase,
                           CerrarSesionUseCase cerrarSesionUseCase,
                           RecuperarContrasenaUseCase recuperarContrasenaUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.iniciarSesionUseCase = iniciarSesionUseCase;
        this.renovarSesionUseCase = renovarSesionUseCase;
        this.cerrarSesionUseCase = cerrarSesionUseCase;
        this.recuperarContrasenaUseCase = recuperarContrasenaUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.RegisterResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        RegistrarUsuarioUseCase.Resultado resultado = registrarUsuarioUseCase.registrar(
            new RegistrarUsuarioUseCase.Command(
                request.nombres(), request.apellidos(), request.tipoDocumento(),
                request.numeroDocumento(), request.email(), request.telefono(), request.password()
            )
        );
        return new AuthDtos.RegisterResponse(resultado.usuarioId(), resultado.email());
    }

    @PostMapping("/login")
    public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        IniciarSesionUseCase.Resultado resultado = iniciarSesionUseCase.iniciarSesion(
            new IniciarSesionUseCase.Command(request.email(), request.password())
        );
        return new AuthDtos.TokenResponse(resultado.accessToken(), resultado.refreshToken());
    }

    @PostMapping("/refresh")
    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        RenovarSesionUseCase.Resultado resultado = renovarSesionUseCase.renovar(
            new RenovarSesionUseCase.Command(request.refreshToken())
        );
        return new AuthDtos.TokenResponse(resultado.accessToken(), resultado.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody AuthDtos.LogoutRequest request) {
        cerrarSesionUseCase.cerrarSesion(new CerrarSesionUseCase.Command(request.refreshToken()));
    }

    /** CA-01: mismo mensaje genérico exista o no el email (ver RecuperarContrasenaService). */
    @PostMapping("/password-reset/request")
    public AuthDtos.PasswordResetRequestResponse solicitarRecuperacion(
            @Valid @RequestBody AuthDtos.PasswordResetRequestRequest request) {
        recuperarContrasenaUseCase.solicitar(request.email());
        return new AuthDtos.PasswordResetRequestResponse(
            "Si el email está registrado, se generó un token de recuperación.");
    }

    @PostMapping("/password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmarRecuperacion(@Valid @RequestBody AuthDtos.PasswordResetConfirmRequest request) {
        recuperarContrasenaUseCase.confirmar(request.token(), request.nuevaPassword());
    }
}
