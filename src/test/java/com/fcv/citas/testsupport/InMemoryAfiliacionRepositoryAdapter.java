package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.AfiliacionRepositoryPort;
import com.fcv.citas.domain.model.Afiliacion;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de prueba de {@link AfiliacionRepositoryPort} (HU-005), sin MySQL. */
public class InMemoryAfiliacionRepositoryAdapter implements AfiliacionRepositoryPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final Map<Long, Afiliacion> porId = new ConcurrentHashMap<>();

    @Override
    public Optional<Afiliacion> buscarVigentePorUsuario(Long usuarioId) {
        return porId.values().stream()
            .filter(afiliacion -> afiliacion.getUsuarioId().equals(usuarioId) && afiliacion.isVigente())
            .findFirst();
    }

    @Override
    public synchronized Afiliacion guardar(Afiliacion afiliacion) {
        Afiliacion aGuardar = afiliacion;
        if (aGuardar.getId() == null) {
            Long nuevoId = secuenciaId.incrementAndGet();
            aGuardar = Afiliacion.reconstruir(nuevoId, afiliacion.getUsuarioId(), afiliacion.getPlanId(),
                afiliacion.getNumeroAfiliacion(), afiliacion.isVigente(), afiliacion.getVigenteDesde());
        }
        porId.put(aGuardar.getId(), aGuardar);
        return aGuardar;
    }
}
