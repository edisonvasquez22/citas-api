package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.AdministrarEpsUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.EpsDtos;
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

/** HU-007 — ADMIN administra el catálogo de EPS. Autorización: ver SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/eps")
public class AdminEpsController {

    private final AdministrarEpsUseCase administrarEpsUseCase;

    public AdminEpsController(AdministrarEpsUseCase administrarEpsUseCase) {
        this.administrarEpsUseCase = administrarEpsUseCase;
    }

    @GetMapping
    public List<EpsDtos.Response> listar() {
        return administrarEpsUseCase.listar().stream().map(AdminEpsController::aResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EpsDtos.Response crear(@Valid @RequestBody EpsDtos.CrearRequest request) {
        var resultado = administrarEpsUseCase.crear(
            new AdministrarEpsUseCase.CrearCommand(request.codigo(), request.nombre()));
        return aResponse(resultado);
    }

    @PutMapping("/{id}")
    public EpsDtos.Response editar(@PathVariable Long id, @Valid @RequestBody EpsDtos.EditarRequest request) {
        var resultado = administrarEpsUseCase.editar(id, new AdministrarEpsUseCase.EditarCommand(request.nombre()));
        return aResponse(resultado);
    }

    @PatchMapping("/{id}/status")
    public EpsDtos.Response cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody EpsDtos.CambiarEstadoRequest request) {
        var resultado = administrarEpsUseCase.cambiarEstado(id, request.activa());
        return aResponse(resultado);
    }

    static EpsDtos.Response aResponse(AdministrarEpsUseCase.Resultado resultado) {
        return new EpsDtos.Response(resultado.id(), resultado.codigo(), resultado.nombre(), resultado.activa());
    }
}
