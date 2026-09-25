package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.Sede;
import java.util.List;
import java.util.Optional;

/** Puerto de solo lectura sobre el catálogo fijo `locations` (HIC/ICV, HU-006). */
public interface LocationRepositoryPort {

    Optional<Sede> buscarPorId(Long id);

    List<Sede> listarActivas();
}
