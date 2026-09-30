package com.fcv.citas.application.port.in;

import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import java.util.List;
import java.util.Set;

/** HU-023 CA-03: historial de estados visible solo dentro del ownership del rol. */
public interface ConsultarHistorialCitaUseCase {

    List<TransicionEstadoCita> consultar(Long usuarioId, Set<RolNombre> roles, Long citaId);
}
