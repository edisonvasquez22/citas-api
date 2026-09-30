package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.EpsRepositoryPort;
import com.fcv.citas.domain.model.Eps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de prueba de {@link EpsRepositoryPort} (HU-007), sin MySQL. */
public class InMemoryEpsRepositoryAdapter implements EpsRepositoryPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final Map<Long, Eps> porId = new ConcurrentHashMap<>();
    private final Map<String, Long> idPorCodigo = new ConcurrentHashMap<>();

    @Override
    public boolean existePorCodigo(String codigo) {
        return codigo != null && idPorCodigo.containsKey(codigo);
    }

    @Override
    public synchronized Eps guardar(Eps eps) {
        Eps aGuardar = eps;
        if (aGuardar.getId() == null) {
            Long nuevoId = secuenciaId.incrementAndGet();
            aGuardar = Eps.reconstruir(nuevoId, eps.getCodigo(), eps.getNombre(), eps.isActiva());
        }
        porId.put(aGuardar.getId(), aGuardar);
        idPorCodigo.put(aGuardar.getCodigo(), aGuardar.getId());
        return aGuardar;
    }

    @Override
    public Optional<Eps> buscarPorId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<Eps> listar() {
        return List.copyOf(porId.values());
    }

    @Override
    public List<Eps> listarActivas() {
        return porId.values().stream().filter(Eps::isActiva).toList();
    }
}
