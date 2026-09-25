package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.GestionarSolicitudesEspecializadasUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.AdminAppointmentDtos;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HU-016 — ADMIN gestiona la bandeja de solicitudes especializadas REQUESTED. */
@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentsController {

    private final GestionarSolicitudesEspecializadasUseCase gestionarSolicitudesUseCase;

    public AdminAppointmentsController(GestionarSolicitudesEspecializadasUseCase gestionarSolicitudesUseCase) {
        this.gestionarSolicitudesUseCase = gestionarSolicitudesUseCase;
    }

    @GetMapping("/requested")
    public List<AdminAppointmentDtos.Resumen> listarSolicitudes(
            @RequestParam(required = false) Long sedeId,
            @RequestParam(required = false) Long profesionalId,
            @RequestParam(required = false) Long especialidadId,
            @RequestParam(required = false) LocalDate fecha) {
        return gestionarSolicitudesUseCase.listarSolicitudes(sedeId, profesionalId, especialidadId, fecha).stream()
            .map(AdminAppointmentsController::aResponse)
            .toList();
    }

    @PostMapping("/{id}/approve")
    public AdminAppointmentDtos.Resumen aprobar(Authentication authentication, @PathVariable Long id) {
        return aResponse(gestionarSolicitudesUseCase.aprobar(Long.valueOf(authentication.getName()), id));
    }

    @PostMapping("/{id}/reject")
    public AdminAppointmentDtos.Resumen rechazar(Authentication authentication, @PathVariable Long id,
                                                    @Valid @RequestBody AdminAppointmentDtos.RechazarRequest request) {
        return aResponse(
            gestionarSolicitudesUseCase.rechazar(Long.valueOf(authentication.getName()), id, request.motivo()));
    }

    private static AdminAppointmentDtos.Resumen aResponse(GestionarSolicitudesEspecializadasUseCase.Resumen r) {
        return new AdminAppointmentDtos.Resumen(r.citaId(), r.pacienteUsuarioId(), r.profesionalId(), r.sedeId(),
            r.especialidadId(), r.estado(), r.inicio(), r.fin(), r.motivoDecision());
    }
}
