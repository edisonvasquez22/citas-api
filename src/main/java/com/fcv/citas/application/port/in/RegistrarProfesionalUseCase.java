package com.fcv.citas.application.port.in;

import java.util.List;
import java.util.Set;

/** HU-010 — Registrar profesional y asignar especialidades/sedes (RF-07). */
public interface RegistrarProfesionalUseCase {

    Resultado registrar(Command command);

    record EspecialidadAsignadaCommand(Long especialidadId, boolean primaria) {}

    record Command(
        String nombres,
        String apellidos,
        String tipoDocumento,
        String numeroDocumento,
        String email,
        String telefono,
        String password,
        String codigoProfesional,
        String matricula,
        List<EspecialidadAsignadaCommand> especialidades,
        Set<Long> sedeIds
    ) {}

    record Resultado(Long profesionalId, Long usuarioId, String codigoProfesional, boolean activo) {}
}
