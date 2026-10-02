package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.GestionarReprogramacionesUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.AdminReprogramacionDtos;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HU-020 — ADMIN gestiona la bandeja de solicitudes de reprogramación PENDING. */
@RestController
@RequestMapping("/api/admin/reschedules")
public class AdminReschedulesController {

    private final GestionarReprogramacionesUseCase gestionarReprogramacionesUseCase;

    public AdminReschedulesController(GestionarReprogramacionesUseCase gestionarReprogramacionesUseCase) {
        this.gestionarReprogramacionesUseCase = gestionarReprogramacionesUseCase;
    }

    @GetMapping
    public List<AdminReprogramacionDtos.Resumen> listarPendientes(
            @RequestParam(required = false) Long sedeId,
            @RequestParam(required = false) Long profesionalId,
            @RequestParam(required = false) Long especialidadId,
            @RequestParam(required = false) LocalDate fecha) {
        return gestionarReprogramacionesUseCase.listarPendientes(sedeId, profesionalId, especialidadId, fecha).stream()
            .map(AdminReschedulesController::aResumen)
            .toList();
    }

    @PostMapping("/{id}/approve")
    public AdminReprogramacionDtos.Resumen aprobar(Authentication authentication, @PathVariable Long id) {
        return aResumen(gestionarReprogramacionesUseCase.aprobar(Long.valueOf(authentication.getName()), id));
    }

    @PostMapping("/{id}/reject")
    public AdminReprogramacionDtos.Resumen rechazar(Authentication authentication, @PathVariable Long id,
                                                       @Valid @RequestBody AdminReprogramacionDtos.RechazarRequest request) {
        return aResumen(
            gestionarReprogramacionesUseCase.rechazar(Long.valueOf(authentication.getName()), id, request.motivo()));
    }

    private static AdminReprogramacionDtos.Resumen aResumen(GestionarReprogramacionesUseCase.Resumen r) {
        return new AdminReprogramacionDtos.Resumen(r.solicitudId(), r.citaId(), r.profesionalId(),
            r.sedeSolicitadaId(), r.estado(), r.inicioAnterior(), r.finAnterior(), r.inicioSolicitado(),
            r.finSolicitado(), r.motivoDecision());
    }
}
