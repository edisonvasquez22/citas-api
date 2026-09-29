package com.fcv.citas.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** HU-021: el PROFESSIONAL autenticado consulta sus propias citas APPROVED por día/semana/sede. */
public interface ConsultarAgendaPropiaUseCase {

    List<Resultado> listar(Long profesionalUsuarioId, Long sedeId, LocalDate desde, LocalDate hasta);

    record Resultado(Long citaId, Long pacienteUsuarioId, Long sedeId, Long especialidadId, LocalDateTime inicio,
                      LocalDateTime fin) {}
}
