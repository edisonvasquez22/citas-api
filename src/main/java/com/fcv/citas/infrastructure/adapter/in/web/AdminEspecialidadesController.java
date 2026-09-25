package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.EspecialidadDtos;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HU-009 — ADMIN administra el catálogo de especialidades. Autorización: ver SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/specialties")
public class AdminEspecialidadesController {

    private final AdministrarEspecialidadesUseCase administrarEspecialidadesUseCase;

    public AdminEspecialidadesController(AdministrarEspecialidadesUseCase administrarEspecialidadesUseCase) {
        this.administrarEspecialidadesUseCase = administrarEspecialidadesUseCase;
    }

    @GetMapping
    public List<EspecialidadDtos.Response> listar() {
        return administrarEspecialidadesUseCase.listar().stream()
            .map(EspecialidadesController::aResponse)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspecialidadDtos.Response crear(@Valid @RequestBody EspecialidadDtos.CrearRequest request) {
        var resultado = administrarEspecialidadesUseCase.crear(new AdministrarEspecialidadesUseCase.CrearCommand(
            request.codigo(), request.nombre(), request.duracionMinutos(), request.general(),
            request.requiereAprobacionAdmin()));
        return EspecialidadesController.aResponse(resultado);
    }

    @PutMapping("/{id}")
    public EspecialidadDtos.Response editar(@PathVariable Long id,
                                             @Valid @RequestBody EspecialidadDtos.EditarRequest request) {
        var resultado = administrarEspecialidadesUseCase.editar(id, new AdministrarEspecialidadesUseCase.EditarCommand(
            request.nombre(), request.duracionMinutos(), request.general(), request.requiereAprobacionAdmin()));
        return EspecialidadesController.aResponse(resultado);
    }

    @PatchMapping("/{id}/status")
    public EspecialidadDtos.Response cambiarEstado(@PathVariable Long id,
                                                     @Valid @RequestBody EspecialidadDtos.CambiarEstadoRequest request) {
        var resultado = administrarEspecialidadesUseCase.cambiarEstado(id, request.activa());
        return EspecialidadesController.aResponse(resultado);
    }
}
