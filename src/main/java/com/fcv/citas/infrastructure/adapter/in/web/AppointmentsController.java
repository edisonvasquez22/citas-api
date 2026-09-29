package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.CancelarCitaUseCase;
import com.fcv.citas.application.port.in.CerrarAtencionUseCase;
import com.fcv.citas.application.port.in.ConsultarMisCitasUseCase;
import com.fcv.citas.application.port.in.SolicitarCitaEspecializadaUseCase;
import com.fcv.citas.application.port.in.SolicitarCitaGeneralUseCase;
import com.fcv.citas.application.port.in.SolicitarReprogramacionUseCase;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.infrastructure.adapter.in.web.dto.AgendaProfesionalDtos;
import com.fcv.citas.infrastructure.adapter.in.web.dto.AppointmentDtos;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-014/HU-015 — el USER autenticado solicita citas generales o especializadas; HU-017 — consulta las propias;
 * HU-018 — las cancela; HU-019 — solicita reprogramarlas; HU-022 — el PROFESSIONAL dueño las cierra
 * (completada/no asistida).
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {

    private final SolicitarCitaGeneralUseCase solicitarCitaGeneralUseCase;
    private final SolicitarCitaEspecializadaUseCase solicitarCitaEspecializadaUseCase;
    private final ConsultarMisCitasUseCase consultarMisCitasUseCase;
    private final CancelarCitaUseCase cancelarCitaUseCase;
    private final CerrarAtencionUseCase cerrarAtencionUseCase;
    private final SolicitarReprogramacionUseCase solicitarReprogramacionUseCase;

    public AppointmentsController(SolicitarCitaGeneralUseCase solicitarCitaGeneralUseCase,
                                   SolicitarCitaEspecializadaUseCase solicitarCitaEspecializadaUseCase,
                                   ConsultarMisCitasUseCase consultarMisCitasUseCase,
                                   CancelarCitaUseCase cancelarCitaUseCase,
                                   CerrarAtencionUseCase cerrarAtencionUseCase,
                                   SolicitarReprogramacionUseCase solicitarReprogramacionUseCase) {
        this.solicitarCitaGeneralUseCase = solicitarCitaGeneralUseCase;
        this.solicitarCitaEspecializadaUseCase = solicitarCitaEspecializadaUseCase;
        this.consultarMisCitasUseCase = consultarMisCitasUseCase;
        this.cancelarCitaUseCase = cancelarCitaUseCase;
        this.cerrarAtencionUseCase = cerrarAtencionUseCase;
        this.solicitarReprogramacionUseCase = solicitarReprogramacionUseCase;
    }

    @PostMapping("/{id}/reschedule")
    public AppointmentDtos.ReprogramarResponse reprogramar(Authentication authentication, @PathVariable Long id,
                                                              @Valid @RequestBody AppointmentDtos.ReprogramarRequest request) {
        var resultado = solicitarReprogramacionUseCase.solicitar(new SolicitarReprogramacionUseCase.Command(
            Long.valueOf(authentication.getName()), id, request.sedeId(), request.fecha(), request.horaInicio()));
        return new AppointmentDtos.ReprogramarResponse(resultado.solicitudId(), resultado.citaId(),
            resultado.estado(), resultado.inicioSolicitado(), resultado.finSolicitado());
    }

    @PostMapping("/{id}/cancel")
    public AgendaProfesionalDtos.CierreResponse cancelar(Authentication authentication, @PathVariable Long id) {
        var resultado = cancelarCitaUseCase.cancelar(Long.valueOf(authentication.getName()), id);
        return new AgendaProfesionalDtos.CierreResponse(resultado.citaId(), resultado.estado());
    }

    @PostMapping("/{id}/complete")
    public AgendaProfesionalDtos.CierreResponse completar(Authentication authentication, @PathVariable Long id) {
        var resultado = cerrarAtencionUseCase.completar(Long.valueOf(authentication.getName()), id);
        return new AgendaProfesionalDtos.CierreResponse(resultado.citaId(), resultado.estado());
    }

    @PostMapping("/{id}/no-show")
    public AgendaProfesionalDtos.CierreResponse marcarNoShow(Authentication authentication, @PathVariable Long id) {
        var resultado = cerrarAtencionUseCase.marcarNoShow(Long.valueOf(authentication.getName()), id);
        return new AgendaProfesionalDtos.CierreResponse(resultado.citaId(), resultado.estado());
    }

    @GetMapping("/mine")
    public List<AppointmentDtos.MiCitaResponse> misCitas(Authentication authentication,
                                                            @RequestParam(required = false) EstadoCita estado,
                                                            @RequestParam(required = false) LocalDate fecha) {
        return consultarMisCitasUseCase.listar(Long.valueOf(authentication.getName()), estado, fecha).stream()
            .map(r -> new AppointmentDtos.MiCitaResponse(r.citaId(), r.sedeId(), r.profesionalId(),
                r.especialidadId(), r.estado(), r.inicio(), r.fin(), r.motivoDecision(),
                r.reprogramacion() == null ? null : new AppointmentDtos.ReprogramacionInfo(
                    r.reprogramacion().solicitudId(), r.reprogramacion().estado(),
                    r.reprogramacion().inicioSolicitado(), r.reprogramacion().finSolicitado(),
                    r.reprogramacion().motivoDecision())))
            .toList();
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
