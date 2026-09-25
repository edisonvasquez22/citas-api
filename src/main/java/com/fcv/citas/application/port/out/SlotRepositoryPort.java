package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.SlotProfesional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto de salida sobre `professional_slots`. La reserva atómica (RN-01) es
 * el mecanismo anti doble-reserva: el adaptador JPA la implementa como un
 * `UPDATE ... WHERE appointment_id IS NULL` (bloqueo de fila real en MySQL);
 * el doble en memoria de pruebas simula la misma atomicidad con
 * compare-and-set por slot (ver testsupport.InMemorySlotRepositoryAdapter).
 */
public interface SlotRepositoryPort {

    /** HU-013: slots libres que coinciden con los filtros dados (todos opcionales salvo la fecha). */
    List<SlotProfesional> buscarLibres(Set<Long> profesionalIds, Long sedeId, LocalDate fecha);

    /**
     * HU-014/HU-015: busca exactamente {@code cantidad} slots consecutivos y
     * libres del profesional/sede indicados, empezando exactamente en
     * {@code inicio}. Vacío si no existen o si alguno ya está reservado.
     */
    Optional<List<Long>> buscarSlotsConsecutivosLibres(Long profesionalId, Long sedeId, LocalDateTime inicio,
                                                         int cantidad);

    /**
     * Intenta asignar {@code citaId} a cada slot de {@code slotIds} solo si
     * sigue libre. Devuelve cuántos se reservaron realmente: si es menor que
     * {@code slotIds.size()}, el llamador perdió la carrera (RN-01/CA-02/CA-03)
     * y debe compensar con {@link #liberarSlotsDeCita(Long)}.
     */
    int reservarAtomicamente(List<Long> slotIds, Long citaId);

    /** RN-09 / compensación de reserva parcial fallida: libera cualquier slot que apunte a esta cita. */
    void liberarSlotsDeCita(Long citaId);
}
