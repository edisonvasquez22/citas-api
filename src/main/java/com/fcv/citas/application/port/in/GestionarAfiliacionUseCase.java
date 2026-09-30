package com.fcv.citas.application.port.in;

import java.util.Optional;

/** HU-005 — Asociar afiliación EPS/plan/régimen (RF-04, parte de afiliación). */
public interface GestionarAfiliacionUseCase {

    Optional<Resultado> consultarVigente(String usuarioId);

    Resultado asociar(String usuarioId, AsociarCommand command);

    record AsociarCommand(Long epsId, Long planId, String numeroAfiliacion) {}

    record Resultado(Long afiliacionId, Long epsId, String epsNombre, Long planId, String planNombre,
                      Long regimenId, String numeroAfiliacion) {}
}
