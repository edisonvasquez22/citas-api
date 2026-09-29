package com.fcv.citas.application.port.in;

/** HU-022: el PROFESSIONAL dueño de la cita la marca como completada o no asistida. */
public interface CerrarAtencionUseCase {

    Resultado completar(Long profesionalUsuarioId, Long citaId);

    Resultado marcarNoShow(Long profesionalUsuarioId, Long citaId);

    record Resultado(Long citaId, String estado) {}
}
