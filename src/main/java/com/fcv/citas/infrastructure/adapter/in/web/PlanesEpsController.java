package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.AdministrarPlanesEpsUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.PlanEpsDtos;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lectura pública (cualquier usuario autenticado) de los planes activos de una EPS (HU-005). */
@RestController
@RequestMapping("/api/eps/{epsId}/plans")
public class PlanesEpsController {

    private final AdministrarPlanesEpsUseCase administrarPlanesEpsUseCase;

    public PlanesEpsController(AdministrarPlanesEpsUseCase administrarPlanesEpsUseCase) {
        this.administrarPlanesEpsUseCase = administrarPlanesEpsUseCase;
    }

    @GetMapping
    public List<PlanEpsDtos.Response> listarActivos(@PathVariable Long epsId) {
        return administrarPlanesEpsUseCase.listarPorEps(epsId).stream()
            .filter(AdministrarPlanesEpsUseCase.Resultado::activo)
            .map(AdminPlanesEpsController::aResponse)
            .toList();
    }
}
