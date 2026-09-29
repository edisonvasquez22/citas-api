package com.fcv.citas.application.port.in;

import com.fcv.citas.domain.model.EstadoCita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** HU-017: el USER autenticado consulta y filtra sus propias citas. */
public interface ConsultarMisCitasUseCase {

    List<Resultado> listar(Long pacienteUsuarioId, EstadoCita estado, LocalDate fecha);

    record Resultado(Long citaId, Long sedeId, Long profesionalId, Long especialidadId, String estado,
                      LocalDateTime inicio, LocalDateTime fin, String motivoDecision,
                      ReprogramacionInfo reprogramacion) {}

    /**
     * HU-019/HU-020: última solicitud de reprogramación conocida para esta cita (cualquiera sea su estado), o
     * {@code null} si nunca se pidió una. El estado de la cita no cambia mientras está PENDING (RN-10), así que
     * esta es la única forma en que el paciente conoce el desenlace de su propia solicitud.
     */
    record ReprogramacionInfo(Long solicitudId, String estado, LocalDateTime inicioSolicitado,
                               LocalDateTime finSolicitado, String motivoDecision) {}
}
