package com.fcv.citas.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/** DTOs de /api/professionals/me/agenda (HU-021) y /api/appointments/{id}/complete|no-show (HU-022). */
public final class AgendaProfesionalDtos {

    private AgendaProfesionalDtos() {}

    public record CitaAgendaResponse(Long citaId, Long pacienteUsuarioId, Long sedeId, Long especialidadId,
                                      LocalDateTime inicio, LocalDateTime fin) {}

    public record CierreResponse(Long citaId, String estado) {}
}
