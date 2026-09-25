package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de prueba de {@link CitaRepositoryPort} (HU-014/HU-015/HU-016), sin MySQL. */
public class InMemoryCitaRepositoryAdapter implements CitaRepositoryPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final Map<Long, Cita> porId = new ConcurrentHashMap<>();

    @Override
    public synchronized Cita guardar(Cita cita) {
        Cita aGuardar = cita;
        if (aGuardar.getId() == null) {
            Long nuevoId = secuenciaId.incrementAndGet();
            aGuardar = Cita.reconstruir(nuevoId, cita.getPacienteUsuarioId(), cita.getProfesionalId(),
                cita.getSedeId(), cita.getEspecialidadId(), cita.getEstado(), cita.getMotivo(), cita.getInicio(),
                cita.getFin(), cita.getCreadoPorUsuarioId(), cita.getAprobadoPorUsuarioId(), cita.getAprobadoEn(),
                cita.getMotivoDecision());
        }
        porId.put(aGuardar.getId(), aGuardar);
        return aGuardar;
    }

    @Override
    public Optional<Cita> buscarPorId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<Cita> listarPorEstado(EstadoCita estado, Long sedeId, Long profesionalId, Long especialidadId,
                                       LocalDate fecha) {
        return porId.values().stream()
            .filter(c -> c.getEstado() == estado)
            .filter(c -> sedeId == null || sedeId.equals(c.getSedeId()))
            .filter(c -> profesionalId == null || profesionalId.equals(c.getProfesionalId()))
            .filter(c -> especialidadId == null || especialidadId.equals(c.getEspecialidadId()))
            .filter(c -> fecha == null || fecha.equals(c.getInicio().toLocalDate()))
            .toList();
    }
}
