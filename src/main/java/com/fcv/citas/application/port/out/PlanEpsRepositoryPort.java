package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.PlanEps;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para el catálogo de planes de EPS (HU-008). */
public interface PlanEpsRepositoryPort {

    boolean existePorEpsYCodigo(Long epsId, String codigo);

    PlanEps guardar(PlanEps plan);

    Optional<PlanEps> buscarPorId(Long id);

    List<PlanEps> listarPorEps(Long epsId);

    List<PlanEps> listarActivosPorEps(Long epsId);
}
