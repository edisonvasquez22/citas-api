package com.fcv.citas.application.port.in;

import java.util.List;
import java.util.Set;

/**
 * Lectura administrativa (ADMIN) del directorio completo de profesionales,
 * incluidos los inactivos — soporte necesario para que HU-011 pueda listar y
 * reactivar/desactivar, no solo dar de alta (HU-010).
 */
public interface ListarProfesionalesAdminUseCase {

    List<Resultado> listarTodos();

    record EspecialidadAsignadaResultado(Long especialidadId, boolean primaria) {}

    record Resultado(Long profesionalId, Long usuarioId, String nombres, String apellidos, String tipoDocumento,
                      String numeroDocumento, String email, String telefono, String codigoProfesional,
                      String matricula, boolean activo, Set<EspecialidadAsignadaResultado> especialidades,
                      Set<Long> sedeIds) {}
}
