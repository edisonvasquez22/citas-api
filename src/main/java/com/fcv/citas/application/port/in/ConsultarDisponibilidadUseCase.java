package com.fcv.citas.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** HU-013 — Consultar disponibilidad de horarios (RF-10, RN-01, RN-05). */
public interface ConsultarDisponibilidadUseCase {

    List<HorarioDisponible> consultar(Consulta consulta);

    record Consulta(Long especialidadId, LocalDate fecha, Long sedeId, Long profesionalId) {}

    record HorarioDisponible(Long profesionalId, Long sedeId, LocalDateTime inicio, LocalDateTime fin) {}
}
