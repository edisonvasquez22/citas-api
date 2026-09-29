package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.ConsultarAgendaPropiaUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.AgendaProfesionalDtos;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HU-021 — el PROFESSIONAL autenticado consulta su propia agenda de citas APPROVED. */
@RestController
@RequestMapping("/api/professionals/me/agenda")
public class AgendaProfesionalController {

    private final ConsultarAgendaPropiaUseCase consultarAgendaPropiaUseCase;

    public AgendaProfesionalController(ConsultarAgendaPropiaUseCase consultarAgendaPropiaUseCase) {
        this.consultarAgendaPropiaUseCase = consultarAgendaPropiaUseCase;
    }

    @GetMapping
    public List<AgendaProfesionalDtos.CitaAgendaResponse> listar(Authentication authentication,
                                                                    @RequestParam(required = false) Long sedeId,
                                                                    @RequestParam(required = false) LocalDate desde,
                                                                    @RequestParam(required = false) LocalDate hasta) {
        return consultarAgendaPropiaUseCase
            .listar(Long.valueOf(authentication.getName()), sedeId, desde, hasta).stream()
            .map(r -> new AgendaProfesionalDtos.CitaAgendaResponse(r.citaId(), r.pacienteUsuarioId(), r.sedeId(),
                r.especialidadId(), r.inicio(), r.fin()))
            .toList();
    }
}
