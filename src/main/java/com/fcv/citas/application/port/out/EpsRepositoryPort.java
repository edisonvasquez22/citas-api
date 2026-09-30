package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.Eps;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para el catálogo de EPS (HU-007). */
public interface EpsRepositoryPort {

    boolean existePorCodigo(String codigo);

    Eps guardar(Eps eps);

    Optional<Eps> buscarPorId(Long id);

    List<Eps> listar();

    List<Eps> listarActivas();
}
