package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.SolicitarCitaEspecializadaUseCase;
import com.fcv.citas.application.port.in.SolicitarCitaGeneralUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.AppointmentDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HU-014/HU-015 — el USER autenticado solicita citas generales o especializadas. */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {

    private final SolicitarCitaGeneralUseCase solicitarCitaGeneralUseCase;
    private final SolicitarCitaEspecializadaUseCase solicitarCitaEspecializadaUseCase;

    public AppointmentsController(SolicitarCitaGeneralUseCase solicitarCitaGeneralUseCase,
                                   SolicitarCitaEspecializadaUseCase solicitarCitaEspecializadaUseCase) {
        this.solicitarCitaGeneralUseCase = solicitarCitaGeneralUseCase;
        this.solicitarCitaEspecializadaUseCase = solicitarCitaEspecializadaUseCase;
    }

    @PostMapping("/general")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDtos.Response solicitarGeneral(Authentication authentication,
                                                        @Valid @RequestBody AppointmentDtos.SolicitarRequest request) {
        var resultado = solicitarCitaGeneralUseCase.solicitar(new SolicitarCitaGeneralUseCase.Command(
            Long.valueOf(authentication.getName()), request.profesionalId(), request.sedeId(),
            request.especialidadId(), request.motivo(), request.fecha(), request.horaInicio()));
        return new AppointmentDtos.Response(resultado.citaId(), resultado.estado(), resultado.inicio(),
            resultado.fin());
    }

    @PostMapping("/specialized")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDtos.Response solicitarEspecializada(Authentication authentication,
                                                              @Valid @RequestBody AppointmentDtos.SolicitarRequest request) {
        var resultado = solicitarCitaEspecializadaUseCase.solicitar(new SolicitarCitaEspecializadaUseCase.Command(
            Long.valueOf(authentication.getName()), request.profesionalId(), request.sedeId(),
            request.especialidadId(), request.motivo(), request.fecha(), request.horaInicio()));
        return new AppointmentDtos.Response(resultado.citaId(), resultado.estado(), resultado.inicio(),
            resultado.fin());
    }
}
