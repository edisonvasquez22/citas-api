package com.fcv.citas.application.port.in;

/** HU-011 — Activar/desactivar profesional (RF-07). */
public interface CambiarEstadoProfesionalUseCase {

    Resultado cambiarEstado(Long profesionalId, boolean activo);

    record Resultado(Long profesionalId, boolean activo) {}
}
