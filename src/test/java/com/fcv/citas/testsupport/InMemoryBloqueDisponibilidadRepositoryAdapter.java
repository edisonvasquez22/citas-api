package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.BloqueDisponibilidadRepositoryPort;
import com.fcv.citas.domain.model.BloqueDisponibilidad;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/** Doble de prueba de {@link BloqueDisponibilidadRepositoryPort} (HU-012), respaldado por {@link InMemoryDisponibilidadStore}. */
public class InMemoryBloqueDisponibilidadRepositoryAdapter implements BloqueDisponibilidadRepositoryPort {

    private final InMemoryDisponibilidadStore store;

    public InMemoryBloqueDisponibilidadRepositoryAdapter(InMemoryDisponibilidadStore store) {
        this.store = store;
    }

    @Override
    public boolean existeSolapamiento(Long profesionalId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                       Long excluirBloqueId) {
        return store.existeSolapamiento(profesionalId, fecha, horaInicio, horaFin, excluirBloqueId);
    }

    @Override
    public BloqueDisponibilidad guardar(BloqueDisponibilidad bloque) {
        return store.crearBloque(bloque);
    }

    @Override
    public Optional<BloqueDisponibilidad> buscarPorId(Long id) {
        return store.buscarBloque(id);
    }

    @Override
    public List<BloqueDisponibilidad> listarPorProfesional(Long profesionalId) {
        return store.listarBloquesPorProfesional(profesionalId);
    }

    @Override
    public boolean tieneSlotsComprometidos(Long bloqueId) {
        return store.tieneSlotsComprometidos(bloqueId);
    }

    @Override
    public BloqueDisponibilidad actualizarHorario(BloqueDisponibilidad bloqueActualizado) {
        return store.actualizarBloque(bloqueActualizado);
    }

    @Override
    public void eliminar(Long bloqueId) {
        store.eliminarBloque(bloqueId);
    }
}
