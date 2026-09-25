package com.fcv.citas.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/** DTOs de /api/availability (HU-013). */
public final class DisponibilidadDtos {

    private DisponibilidadDtos() {}

    public record HorarioResponse(Long profesionalId, Long sedeId, LocalDateTime inicio, LocalDateTime fin) {}
}
