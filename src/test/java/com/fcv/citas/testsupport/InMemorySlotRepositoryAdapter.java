package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.domain.model.SlotProfesional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Doble de prueba de {@link SlotRepositoryPort} (HU-013/HU-014/HU-015), respaldado por {@link InMemoryDisponibilidadStore}. */
public class InMemorySlotRepositoryAdapter implements SlotRepositoryPort {

    private final InMemoryDisponibilidadStore store;

    public InMemorySlotRepositoryAdapter(InMemoryDisponibilidadStore store) {
        this.store = store;
    }

    @Override
    public List<SlotProfesional> buscarLibres(Set<Long> profesionalIds, Long sedeId, LocalDate fecha) {
        return store.buscarLibres(profesionalIds, sedeId, fecha);
    }

    @Override
    public Optional<List<Long>> buscarSlotsConsecutivosLibres(Long profesionalId, Long sedeId, LocalDateTime inicio,
                                                                int cantidad) {
        return store.buscarSlotsConsecutivosLibres(profesionalId, sedeId, inicio, cantidad);
    }

    @Override
    public int reservarAtomicamente(List<Long> slotIds, Long citaId) {
        return store.reservarAtomicamente(slotIds, citaId);
    }

    @Override
    public void liberarSlotsDeCita(Long citaId) {
        store.liberarSlotsDeCita(citaId);
    }
}
