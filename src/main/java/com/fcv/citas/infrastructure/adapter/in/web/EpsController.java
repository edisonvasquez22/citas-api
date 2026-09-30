package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.AdministrarEpsUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.EpsDtos;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lectura pública (cualquier usuario autenticado) del catálogo activo de EPS, para el flujo de afiliación (HU-005). */
@RestController
@RequestMapping("/api/eps")
public class EpsController {

    private final AdministrarEpsUseCase administrarEpsUseCase;

    public EpsController(AdministrarEpsUseCase administrarEpsUseCase) {
        this.administrarEpsUseCase = administrarEpsUseCase;
    }

    @GetMapping
    public List<EpsDtos.Response> listarActivas() {
        return administrarEpsUseCase.listar().stream()
            .filter(AdministrarEpsUseCase.Resultado::activa)
            .map(AdminEpsController::aResponse)
            .toList();
    }
}
