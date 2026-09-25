package com.fcv.citas.infrastructure.adapter.in.web.dto;

import java.util.Set;

/** DTOs de /api/professionals (lectura pública, soporte de HU-013/014/015). */
public final class ProfesionalPublicoDtos {

    private ProfesionalPublicoDtos() {}

    public record Response(Long profesionalId, String nombreCompleto, Set<Long> especialidadIds, Set<Long> sedeIds) {}
}
