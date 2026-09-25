package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.LocationRepositoryPort;
import com.fcv.citas.domain.model.Sede;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Doble de prueba de {@link LocationRepositoryPort}, precargado con las
 * mismas sedes fijas de {@code V2__seed_catalogos_fijos.sql} (HIC=1, ICV=2)
 * para que las pruebas de integración puedan referenciarlas igual que en producción.
 */
public class InMemoryLocationRepositoryAdapter implements LocationRepositoryPort {

    private final Map<Long, Sede> porId = new ConcurrentHashMap<>();

    public InMemoryLocationRepositoryAdapter() {
        porId.put(1L, new Sede(1L, "HIC", "Hospital Internacional de Colombia (HIC)", true));
        porId.put(2L, new Sede(2L, "ICV", "Fundación Cardiovascular de Colombia - Instituto Cardiovascular (ICV)", true));
    }

    @Override
    public Optional<Sede> buscarPorId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<Sede> listarActivas() {
        return porId.values().stream().filter(Sede::activa).toList();
    }
}
