package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.ListarProfesionalesUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.ProfesionalPublicoDtos;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lectura pública (cualquier usuario autenticado) del directorio de profesionales activos. Ver HU-013/014/015. */
@RestController
@RequestMapping("/api/professionals")
public class ProfesionalesController {

    private final ListarProfesionalesUseCase listarProfesionalesUseCase;

    public ProfesionalesController(ListarProfesionalesUseCase listarProfesionalesUseCase) {
        this.listarProfesionalesUseCase = listarProfesionalesUseCase;
    }

    @GetMapping
    public List<ProfesionalPublicoDtos.Response> listar() {
        return listarProfesionalesUseCase.listarActivos().stream()
            .map(r -> new ProfesionalPublicoDtos.Response(r.profesionalId(), r.nombreCompleto(),
                r.especialidadIds(), r.sedeIds()))
            .toList();
    }
}
