package com.fcv.citas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/** DTOs de /api/admin/appointments/** (HU-016). */
public final class AdminAppointmentDtos {

    private AdminAppointmentDtos() {}

    public record RechazarRequest(@NotBlank String motivo) {}

    public record Resumen(Long citaId, Long pacienteUsuarioId, Long profesionalId, Long sedeId, Long especialidadId,
                           String estado, LocalDateTime inicio, LocalDateTime fin, String motivoDecision) {}
}
