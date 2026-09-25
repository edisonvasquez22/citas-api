package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.Especialidad;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para el catálogo de especialidades (HU-009). */
public interface EspecialidadRepositoryPort {

    boolean existePorCodigo(String codigo);

    Especialidad guardar(Especialidad especialidad);

    Optional<Especialidad> buscarPorId(Long id);

    List<Especialidad> listar();

    List<Especialidad> listarActivas();
}
