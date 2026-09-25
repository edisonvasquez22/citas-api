package com.fcv.citas.application.port.in;

import java.util.List;
import java.util.Set;

/**
 * Lectura pública (cualquier usuario autenticado) del directorio de
 * profesionales activos — soporte necesario para que HU-013/HU-014/HU-015
 * puedan mostrar a quién corresponde cada horario, no solo su id.
 */
public interface ListarProfesionalesUseCase {

    List<Resultado> listarActivos();

    record Resultado(Long profesionalId, String nombreCompleto, Set<Long> especialidadIds, Set<Long> sedeIds) {}
}
