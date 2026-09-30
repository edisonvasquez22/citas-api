package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.GestionarAfiliacionUseCase;
import com.fcv.citas.application.port.in.GestionarPerfilUseCase;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.infrastructure.adapter.in.web.dto.UserDtos;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-004 (perfil propio) y HU-005 (afiliación EPS/plan propia). El ownership
 * queda garantizado por diseño: el id siempre viene de
 * {@code authentication.getName()} (JWT), nunca de un path/query param que el
 * cliente pueda manipular para ver/editar a otro usuario.
 */
@RestController
@RequestMapping("/api/users/me")
public class UsersController {

    private final GestionarPerfilUseCase gestionarPerfilUseCase;
    private final GestionarAfiliacionUseCase gestionarAfiliacionUseCase;

    public UsersController(GestionarPerfilUseCase gestionarPerfilUseCase,
                            GestionarAfiliacionUseCase gestionarAfiliacionUseCase) {
        this.gestionarPerfilUseCase = gestionarPerfilUseCase;
        this.gestionarAfiliacionUseCase = gestionarAfiliacionUseCase;
    }

    @GetMapping
    public UserDtos.PerfilResponse consultarPerfil(Authentication authentication) {
        return aPerfilResponse(gestionarPerfilUseCase.consultar(authentication.getName()));
    }

    @PatchMapping
    public UserDtos.PerfilResponse actualizarPerfil(Authentication authentication,
                                                      @Valid @RequestBody UserDtos.ActualizarPerfilRequest request) {
        var resultado = gestionarPerfilUseCase.actualizar(authentication.getName(),
            new GestionarPerfilUseCase.ActualizarCommand(request.nombres(), request.apellidos(), request.telefono()));
        return aPerfilResponse(resultado);
    }

    @GetMapping("/afiliacion")
    public UserDtos.AfiliacionResponse consultarAfiliacion(Authentication authentication) {
        return gestionarAfiliacionUseCase.consultarVigente(authentication.getName())
            .map(UsersController::aAfiliacionResponse)
            .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no tiene una afiliación registrada"));
    }

    @PutMapping("/afiliacion")
    public UserDtos.AfiliacionResponse asociarAfiliacion(Authentication authentication,
            @Valid @RequestBody UserDtos.AsociarAfiliacionRequest request) {
        var resultado = gestionarAfiliacionUseCase.asociar(authentication.getName(),
            new GestionarAfiliacionUseCase.AsociarCommand(request.epsId(), request.planId(),
                request.numeroAfiliacion()));
        return aAfiliacionResponse(resultado);
    }

    private static UserDtos.PerfilResponse aPerfilResponse(GestionarPerfilUseCase.Resultado resultado) {
        return new UserDtos.PerfilResponse(resultado.id(), resultado.nombres(), resultado.apellidos(),
            resultado.tipoDocumento(), resultado.numeroDocumento(), resultado.email(), resultado.telefono());
    }

    private static UserDtos.AfiliacionResponse aAfiliacionResponse(GestionarAfiliacionUseCase.Resultado resultado) {
        return new UserDtos.AfiliacionResponse(resultado.afiliacionId(), resultado.epsId(), resultado.epsNombre(),
            resultado.planId(), resultado.planNombre(), resultado.regimenId(), resultado.numeroAfiliacion());
    }
}
