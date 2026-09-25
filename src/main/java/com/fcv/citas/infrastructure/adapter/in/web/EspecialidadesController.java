package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.EspecialidadDtos;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lectura pública (cualquier usuario autenticado) del catálogo activo de especialidades. Ver HU-024. */
@RestController
@RequestMapping("/api/specialties")
public class EspecialidadesController {

    private final AdministrarEspecialidadesUseCase administrarEspecialidadesUseCase;

    public EspecialidadesController(AdministrarEspecialidadesUseCase administrarEspecialidadesUseCase) {
        this.administrarEspecialidadesUseCase = administrarEspecialidadesUseCase;
    }

    @GetMapping
    public List<EspecialidadDtos.Response> listarActivas() {
        return administrarEspecialidadesUseCase.listar().stream()
            .filter(AdministrarEspecialidadesUseCase.Resultado::activa)
            .map(EspecialidadesController::aResponse)
            .toList();
    }

    static EspecialidadDtos.Response aResponse(AdministrarEspecialidadesUseCase.Resultado resultado) {
        return new EspecialidadDtos.Response(resultado.id(), resultado.codigo(), resultado.nombre(),
            resultado.duracionMinutos(), resultado.general(), resultado.requiereAprobacionAdmin(),
            resultado.activa());
    }
}
