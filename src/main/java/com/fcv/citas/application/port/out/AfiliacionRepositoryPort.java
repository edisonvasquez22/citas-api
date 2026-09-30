package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.Afiliacion;
import java.util.Optional;

/** Puerto de salida para la afiliación EPS/plan del usuario (HU-005). */
public interface AfiliacionRepositoryPort {

    Optional<Afiliacion> buscarVigentePorUsuario(Long usuarioId);

    Optional<Afiliacion> buscarPorUsuarioPlanYNumero(Long usuarioId, Long planId, String numeroAfiliacion);

    Afiliacion guardar(Afiliacion afiliacion);
}
