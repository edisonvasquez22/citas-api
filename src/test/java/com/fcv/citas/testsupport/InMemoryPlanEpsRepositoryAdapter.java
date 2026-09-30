package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.PlanEpsRepositoryPort;
import com.fcv.citas.domain.model.PlanEps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de prueba de {@link PlanEpsRepositoryPort} (HU-008), sin MySQL. */
public class InMemoryPlanEpsRepositoryAdapter implements PlanEpsRepositoryPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final Map<Long, PlanEps> porId = new ConcurrentHashMap<>();

    @Override
    public boolean existePorEpsYCodigo(Long epsId, String codigo) {
        return porId.values().stream()
            .anyMatch(plan -> plan.getEpsId().equals(epsId) && plan.getCodigo().equals(codigo));
    }

    @Override
    public synchronized PlanEps guardar(PlanEps plan) {
        PlanEps aGuardar = plan;
        if (aGuardar.getId() == null) {
            Long nuevoId = secuenciaId.incrementAndGet();
            aGuardar = PlanEps.reconstruir(nuevoId, plan.getEpsId(), plan.getRegimenId(), plan.getCodigo(),
                plan.getNombre(), plan.isActivo());
        }
        porId.put(aGuardar.getId(), aGuardar);
        return aGuardar;
    }

    @Override
    public Optional<PlanEps> buscarPorId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<PlanEps> listarPorEps(Long epsId) {
        return porId.values().stream().filter(plan -> plan.getEpsId().equals(epsId)).toList();
    }

    @Override
    public List<PlanEps> listarActivosPorEps(Long epsId) {
        return porId.values().stream()
            .filter(plan -> plan.getEpsId().equals(epsId) && plan.isActivo())
            .toList();
    }
}
