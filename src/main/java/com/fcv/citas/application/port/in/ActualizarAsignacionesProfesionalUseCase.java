package com.fcv.citas.application.port.in;

import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase.EspecialidadAsignadaCommand;
import java.util.List;
import java.util.Set;

/** RF-07: el ADMIN cambia especialidades (con una primaria) y sedes de un profesional ya registrado. */
public interface ActualizarAsignacionesProfesionalUseCase {

    Resultado actualizar(Long profesionalId, List<EspecialidadAsignadaCommand> especialidades, Set<Long> sedeIds);

    record Resultado(Long profesionalId, List<EspecialidadAsignadaCommand> especialidades, Set<Long> sedeIds) {}
}
