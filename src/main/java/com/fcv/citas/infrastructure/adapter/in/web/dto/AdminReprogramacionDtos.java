package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/** DTOs de /api/admin/reschedules (HU-020). */
public final class AdminReprogramacionDtos {

    private AdminReprogramacionDtos() {}

    public record RechazarRequest(@NotBlank String motivo) {}

    public record Resumen(Long solicitudId, Long citaId, Long profesionalId, Long sedeSolicitadaId, String estado,
                           LocalDateTime inicioAnterior, LocalDateTime finAnterior, LocalDateTime inicioSolicitado,
                           LocalDateTime finSolicitado, String motivoDecision) {}
}
