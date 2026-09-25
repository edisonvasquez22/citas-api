package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.CambiarEstadoProfesionalUseCase;
import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.ProfesionalDtos;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HU-010/HU-011 — ADMIN registra y activa/desactiva profesionales. Autorización: ver SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/professionals")
public class AdminProfesionalesController {

    private final RegistrarProfesionalUseCase registrarProfesionalUseCase;
    private final CambiarEstadoProfesionalUseCase cambiarEstadoProfesionalUseCase;

    public AdminProfesionalesController(RegistrarProfesionalUseCase registrarProfesionalUseCase,
                                         CambiarEstadoProfesionalUseCase cambiarEstadoProfesionalUseCase) {
        this.registrarProfesionalUseCase = registrarProfesionalUseCase;
        this.cambiarEstadoProfesionalUseCase = cambiarEstadoProfesionalUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfesionalDtos.Response registrar(@Valid @RequestBody ProfesionalDtos.RegistrarRequest request) {
        List<RegistrarProfesionalUseCase.EspecialidadAsignadaCommand> especialidades = request.especialidades()
            .stream()
            .map(e -> new RegistrarProfesionalUseCase.EspecialidadAsignadaCommand(e.especialidadId(), e.primaria()))
            .toList();

        RegistrarProfesionalUseCase.Resultado resultado = registrarProfesionalUseCase.registrar(
            new RegistrarProfesionalUseCase.Command(request.nombres(), request.apellidos(), request.tipoDocumento(),
                request.numeroDocumento(), request.email(), request.telefono(), request.password(),
                request.codigoProfesional(), request.matricula(), especialidades, request.sedeIds()));

        return new ProfesionalDtos.Response(resultado.profesionalId(), resultado.usuarioId(),
            resultado.codigoProfesional(), resultado.activo());
    }

    @PatchMapping("/{id}/status")
    public ProfesionalDtos.Response cambiarEstado(@PathVariable Long id,
                                                    @Valid @RequestBody ProfesionalDtos.CambiarEstadoRequest request) {
        CambiarEstadoProfesionalUseCase.Resultado resultado =
            cambiarEstadoProfesionalUseCase.cambiarEstado(id, request.activo());
        return new ProfesionalDtos.Response(resultado.profesionalId(), null, null, resultado.activo());
    }
}
