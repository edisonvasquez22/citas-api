package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.Profesional;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para profesionales y sus asignaciones de especialidad/sede (HU-010/HU-011). */
public interface ProfesionalRepositoryPort {

    boolean existeCodigoProfesional(String codigoProfesional);

    boolean existeMatricula(String matricula);

    Profesional guardar(Profesional profesional);

    Optional<Profesional> buscarPorId(Long id);

    Optional<Profesional> buscarPorUsuarioId(Long usuarioId);

    /** HU-013: candidatos para resolver disponibilidad por especialidad/sede. */
    List<Profesional> listarActivos();
}
