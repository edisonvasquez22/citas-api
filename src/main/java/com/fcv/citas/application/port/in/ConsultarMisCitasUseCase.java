package com.fcv.citas.application.port.in;

import com.fcv.citas.domain.model.EstadoCita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** HU-017: el USER autenticado consulta y filtra sus propias citas. */
public interface ConsultarMisCitasUseCase {

    List<Resultado> listar(Long pacienteUsuarioId, EstadoCita estado, LocalDate fecha);

    record Resultado(Long citaId, Long sedeId, Long profesionalId, Long especialidadId, String estado,
                      LocalDateTime inicio, LocalDateTime fin, String motivoDecision) {}
}
