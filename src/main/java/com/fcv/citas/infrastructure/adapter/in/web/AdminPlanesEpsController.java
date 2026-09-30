package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.AdministrarPlanesEpsUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.PlanEpsDtos;
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

/** HU-008 — ADMIN administra los planes de una EPS. Autorización: ver SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/eps/{epsId}/plans")
public class AdminPlanesEpsController {

    private final AdministrarPlanesEpsUseCase administrarPlanesEpsUseCase;

    public AdminPlanesEpsController(AdministrarPlanesEpsUseCase administrarPlanesEpsUseCase) {
        this.administrarPlanesEpsUseCase = administrarPlanesEpsUseCase;
    }

    @GetMapping
    public List<PlanEpsDtos.Response> listar(@PathVariable Long epsId) {
        return administrarPlanesEpsUseCase.listarPorEps(epsId).stream()
            .map(AdminPlanesEpsController::aResponse)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanEpsDtos.Response crear(@PathVariable Long epsId, @Valid @RequestBody PlanEpsDtos.CrearRequest request) {
        var resultado = administrarPlanesEpsUseCase.crear(epsId, new AdministrarPlanesEpsUseCase.CrearCommand(
            request.regimenId(), request.codigo(), request.nombre()));
        return aResponse(resultado);
    }

    @PutMapping("/{id}")
    public PlanEpsDtos.Response editar(@PathVariable Long epsId, @PathVariable Long id,
                                        @Valid @RequestBody PlanEpsDtos.EditarRequest request) {
        var resultado = administrarPlanesEpsUseCase.editar(id,
            new AdministrarPlanesEpsUseCase.EditarCommand(request.nombre()));
        return aResponse(resultado);
    }

    @PatchMapping("/{id}/status")
    public PlanEpsDtos.Response cambiarEstado(@PathVariable Long epsId, @PathVariable Long id,
                                               @Valid @RequestBody PlanEpsDtos.CambiarEstadoRequest request) {
        var resultado = administrarPlanesEpsUseCase.cambiarEstado(id, request.activo());
        return aResponse(resultado);
    }

    static PlanEpsDtos.Response aResponse(AdministrarPlanesEpsUseCase.Resultado resultado) {
        return new PlanEpsDtos.Response(resultado.id(), resultado.epsId(), resultado.regimenId(), resultado.codigo(),
            resultado.nombre(), resultado.activo());
    }
}
