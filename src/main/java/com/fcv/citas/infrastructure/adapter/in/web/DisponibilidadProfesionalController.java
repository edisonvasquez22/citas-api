package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.GestionarBloquesDisponibilidadUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.BloqueDisponibilidadDtos;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HU-012 — el PROFESSIONAL autenticado gestiona sus propios bloques de disponibilidad. */
@RestController
@RequestMapping("/api/professionals/me/availability-blocks")
public class DisponibilidadProfesionalController {

    private final GestionarBloquesDisponibilidadUseCase gestionarBloquesUseCase;

    public DisponibilidadProfesionalController(GestionarBloquesDisponibilidadUseCase gestionarBloquesUseCase) {
        this.gestionarBloquesUseCase = gestionarBloquesUseCase;
    }

    @GetMapping
    public List<BloqueDisponibilidadDtos.Response> listar(Authentication authentication) {
        return gestionarBloquesUseCase.listarPropios(usuarioId(authentication)).stream().map(this::aResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueDisponibilidadDtos.Response crear(Authentication authentication,
                                                      @Valid @RequestBody BloqueDisponibilidadDtos.CrearRequest request) {
        var resultado = gestionarBloquesUseCase.crear(new GestionarBloquesDisponibilidadUseCase.CrearCommand(
            usuarioId(authentication), request.sedeId(), request.fecha(), request.horaInicio(), request.horaFin()));
        return aResponse(resultado);
    }

    @PutMapping("/{id}")
    public BloqueDisponibilidadDtos.Response editar(Authentication authentication, @PathVariable Long id,
                                                       @Valid @RequestBody BloqueDisponibilidadDtos.EditarRequest request) {
        var resultado = gestionarBloquesUseCase.editar(new GestionarBloquesDisponibilidadUseCase.EditarCommand(
            usuarioId(authentication), id, request.sedeId(), request.fecha(), request.horaInicio(), request.horaFin()));
        return aResponse(resultado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(Authentication authentication, @PathVariable Long id) {
        gestionarBloquesUseCase.eliminar(usuarioId(authentication), id);
    }

    private Long usuarioId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    private BloqueDisponibilidadDtos.Response aResponse(GestionarBloquesDisponibilidadUseCase.Resultado resultado) {
        return new BloqueDisponibilidadDtos.Response(resultado.id(), resultado.profesionalId(), resultado.sedeId(),
            resultado.fecha(), resultado.horaInicio(), resultado.horaFin(), resultado.activo());
    }
}
