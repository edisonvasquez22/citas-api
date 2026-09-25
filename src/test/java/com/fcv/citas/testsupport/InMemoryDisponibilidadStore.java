package com.fcv.citas.testsupport;

import com.fcv.citas.domain.model.BloqueDisponibilidad;
import com.fcv.citas.domain.model.SlotProfesional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Almacén compartido entre {@link InMemoryBloqueDisponibilidadRepositoryAdapter}
 * y {@link InMemorySlotRepositoryAdapter} (misma pareja de tablas en la base
 * real: `availability_blocks` + `professional_slots`). La reserva atómica usa
 * compare-and-set por slot para simular, sin MySQL, el mismo bloqueo de fila
 * que un `UPDATE ... WHERE appointment_id IS NULL` real (RN-01).
 */
public class InMemoryDisponibilidadStore {

    private final AtomicLong secuenciaBloque = new AtomicLong(0);
    private final AtomicLong secuenciaSlot = new AtomicLong(0);
    private final Map<Long, BloqueDisponibilidad> bloques = new ConcurrentHashMap<>();
    private final Map<Long, SlotEntry> slots = new ConcurrentHashMap<>();

    private static final class SlotEntry {
        final long id;
        final long bloqueId;
        final long profesionalId;
        final long sedeId;
        final LocalDateTime inicio;
        final LocalDateTime fin;
        final AtomicReference<Long> citaId = new AtomicReference<>();

        SlotEntry(long id, long bloqueId, long profesionalId, long sedeId, LocalDateTime inicio, LocalDateTime fin) {
            this.id = id;
            this.bloqueId = bloqueId;
            this.profesionalId = profesionalId;
            this.sedeId = sedeId;
            this.inicio = inicio;
            this.fin = fin;
        }

        SlotProfesional aDominio() {
            return new SlotProfesional(id, bloqueId, profesionalId, sedeId, inicio, fin, citaId.get());
        }
    }

    public synchronized BloqueDisponibilidad crearBloque(BloqueDisponibilidad bloque) {
        long id = secuenciaBloque.incrementAndGet();
        BloqueDisponibilidad guardado = BloqueDisponibilidad.reconstruir(id, bloque.getProfesionalId(),
            bloque.getSedeId(), bloque.getFecha(), bloque.getHoraInicio(), bloque.getHoraFin(), true);
        bloques.put(id, guardado);
        crearSlotsPara(guardado);
        return guardado;
    }

    public synchronized BloqueDisponibilidad actualizarBloque(BloqueDisponibilidad actualizado) {
        bloques.put(actualizado.getId(), actualizado);
        slots.values().removeIf(s -> s.bloqueId == actualizado.getId());
        crearSlotsPara(actualizado);
        return actualizado;
    }

    public synchronized void eliminarBloque(long bloqueId) {
        bloques.remove(bloqueId);
        slots.values().removeIf(s -> s.bloqueId == bloqueId);
    }

    private void crearSlotsPara(BloqueDisponibilidad bloque) {
        for (BloqueDisponibilidad.RangoSlot rango : bloque.generarSlots()) {
            long slotId = secuenciaSlot.incrementAndGet();
            slots.put(slotId, new SlotEntry(slotId, bloque.getId(), bloque.getProfesionalId(), bloque.getSedeId(),
                rango.inicio(), rango.fin()));
        }
    }

    public Optional<BloqueDisponibilidad> buscarBloque(long id) {
        return Optional.ofNullable(bloques.get(id));
    }

    public List<BloqueDisponibilidad> listarBloquesPorProfesional(long profesionalId) {
        return bloques.values().stream().filter(b -> b.getProfesionalId() == profesionalId).toList();
    }

    public boolean existeSolapamiento(long profesionalId, LocalDate fecha, java.time.LocalTime horaInicio,
                                       java.time.LocalTime horaFin, Long excluirBloqueId) {
        BloqueDisponibilidad candidato = BloqueDisponibilidad.reconstruir(-1L, profesionalId, 0L, fecha, horaInicio,
            horaFin, true);
        return bloques.values().stream()
            .filter(b -> b.getProfesionalId() == profesionalId)
            .filter(b -> excluirBloqueId == null || !b.getId().equals(excluirBloqueId))
            .anyMatch(candidato::seSolapaCon);
    }

    public boolean tieneSlotsComprometidos(long bloqueId) {
        return slots.values().stream().anyMatch(s -> s.bloqueId == bloqueId && s.citaId.get() != null);
    }

    public List<SlotProfesional> buscarLibres(Set<Long> profesionalIds, Long sedeId, LocalDate fecha) {
        return slots.values().stream()
            .filter(s -> s.citaId.get() == null)
            .filter(s -> profesionalIds.contains(s.profesionalId))
            .filter(s -> sedeId == null || s.sedeId == sedeId)
            .filter(s -> s.inicio.toLocalDate().equals(fecha))
            .map(SlotEntry::aDominio)
            .toList();
    }

    public Optional<List<Long>> buscarSlotsConsecutivosLibres(long profesionalId, long sedeId, LocalDateTime inicio,
                                                                int cantidad) {
        List<SlotEntry> candidatos = slots.values().stream()
            .filter(s -> s.profesionalId == profesionalId && s.sedeId == sedeId)
            .filter(s -> !s.inicio.isBefore(inicio))
            .sorted(Comparator.comparing(s -> s.inicio))
            .limit(cantidad)
            .toList();
        if (candidatos.size() < cantidad) {
            return Optional.empty();
        }
        if (!candidatos.get(0).inicio.equals(inicio)) {
            return Optional.empty();
        }
        for (SlotEntry slot : candidatos) {
            if (slot.citaId.get() != null) {
                return Optional.empty();
            }
        }
        for (int i = 0; i < candidatos.size() - 1; i++) {
            if (!candidatos.get(i).fin.equals(candidatos.get(i + 1).inicio)) {
                return Optional.empty();
            }
        }
        List<Long> ids = new ArrayList<>();
        candidatos.forEach(s -> ids.add(s.id));
        return Optional.of(ids);
    }

    public int reservarAtomicamente(List<Long> slotIds, Long citaId) {
        int reservados = 0;
        for (Long id : slotIds) {
            SlotEntry entry = slots.get(id);
            if (entry != null && entry.citaId.compareAndSet(null, citaId)) {
                reservados++;
            }
        }
        return reservados;
    }

    public void liberarSlotsDeCita(Long citaId) {
        slots.values().forEach(s -> s.citaId.compareAndSet(citaId, null));
    }
}
