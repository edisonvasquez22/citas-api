package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.EspecialidadRepositoryPort;
import com.fcv.citas.domain.model.Especialidad;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de prueba de {@link EspecialidadRepositoryPort} (HU-009), sin MySQL. */
public class InMemoryEspecialidadRepositoryAdapter implements EspecialidadRepositoryPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final Map<Long, Especialidad> porId = new ConcurrentHashMap<>();
    private final Map<String, Long> idPorCodigo = new ConcurrentHashMap<>();

    @Override
    public boolean existePorCodigo(String codigo) {
        return codigo != null && idPorCodigo.containsKey(codigo);
    }

    @Override
    public synchronized Especialidad guardar(Especialidad especialidad) {
        Especialidad aGuardar = especialidad;
        if (aGuardar.getId() == null) {
            Long nuevoId = secuenciaId.incrementAndGet();
            aGuardar = Especialidad.reconstruir(nuevoId, especialidad.getCodigo(), especialidad.getNombre(),
                especialidad.getDuracionMinutos(), especialidad.isGeneral(), especialidad.isRequiereAprobacionAdmin(),
                especialidad.isActiva());
        }
        porId.put(aGuardar.getId(), aGuardar);
        idPorCodigo.put(aGuardar.getCodigo(), aGuardar.getId());
        return aGuardar;
    }

    @Override
    public Optional<Especialidad> buscarPorId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<Especialidad> listar() {
        return List.copyOf(porId.values());
    }

    @Override
    public List<Especialidad> listarActivas() {
        return porId.values().stream().filter(Especialidad::isActiva).toList();
    }
}
