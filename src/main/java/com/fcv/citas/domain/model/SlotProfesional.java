package com.fcv.citas.domain.model;

import java.time.LocalDateTime;

/**
 * Slot atómico de 30 minutos ya persistido (tabla `professional_slots`).
 * {@code citaId} es {@code null} mientras está libre; lo asigna la reserva
 * atómica de {@code SlotRepositoryPort.reservarAtomicamente} (HU-014/HU-015).
 */
public record SlotProfesional(Long id, Long bloqueId, Long profesionalId, Long sedeId, LocalDateTime inicio,
                               LocalDateTime fin, Long citaId) {

    public boolean estaLibre() {
        return citaId == null;
    }
}
