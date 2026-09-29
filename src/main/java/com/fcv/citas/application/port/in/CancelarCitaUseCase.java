package com.fcv.citas.application.port.in;

/** HU-018: el USER autenticado cancela una cita propia, futura y no terminal. */
public interface CancelarCitaUseCase {

    Resultado cancelar(Long pacienteUsuarioId, Long citaId);

    record Resultado(Long citaId, String estado) {}
}
