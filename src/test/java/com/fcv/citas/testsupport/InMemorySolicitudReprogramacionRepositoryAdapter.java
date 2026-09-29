package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.SolicitudReprogramacionRepositoryPort;
import com.fcv.citas.domain.model.EstadoSolicitudReprogramacion;
import com.fcv.citas.domain.model.SolicitudReprogramacion;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de prueba de {@link SolicitudReprogramacionRepositoryPort} (HU-019/HU-020), sin MySQL. */
public class InMemorySolicitudReprogramacionRepositoryAdapter implements SolicitudReprogramacionRepositoryPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final Map<Long, SolicitudReprogramacion> porId = new ConcurrentHashMap<>();

    @Override
    public synchronized SolicitudReprogramacion guardar(SolicitudReprogramacion solicitud) {
        SolicitudReprogramacion aGuardar = solicitud;
        if (aGuardar.getId() == null) {
            Long nuevoId = secuenciaId.incrementAndGet();
            aGuardar = SolicitudReprogramacion.reconstruir(nuevoId, solicitud.getCitaId(),
                solicitud.getSolicitadoPorUsuarioId(), solicitud.getSedeSolicitadaId(), solicitud.getEstado(),
                solicitud.getInicioAnterior(), solicitud.getFinAnterior(), solicitud.getInicioSolicitado(),
                solicitud.getFinSolicitado(), solicitud.getMotivoDecision(), solicitud.getDecididoPorUsuarioId(),
                solicitud.getDecididoEn());
        }
        porId.put(aGuardar.getId(), aGuardar);
        return aGuardar;
    }

    @Override
    public Optional<SolicitudReprogramacion> buscarPorId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<SolicitudReprogramacion> listarPorEstado(EstadoSolicitudReprogramacion estado) {
        return porId.values().stream().filter(s -> s.getEstado() == estado).toList();
    }

    @Override
    public Optional<SolicitudReprogramacion> buscarUltimaPorCita(Long citaId) {
        return porId.values().stream().filter(s -> s.getCitaId().equals(citaId))
            .max(java.util.Comparator.comparing(SolicitudReprogramacion::getId));
    }
}
