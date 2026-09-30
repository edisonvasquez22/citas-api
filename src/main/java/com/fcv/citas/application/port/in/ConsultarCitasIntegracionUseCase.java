package com.fcv.citas.application.port.in;

import com.fcv.citas.domain.model.CitaNotificable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Consultas de solo lectura para las automatizaciones n8n (WF-001 y WF-003). */
public interface ConsultarCitasIntegracionUseCase {

    /** WF-001: citas APPROVED que inician en [desde, hasta). */
    List<CitaNotificable> citasParaRecordatorio(LocalDateTime desde, LocalDateTime hasta);

    /** WF-003: resumen de las citas del día. */
    ResumenDiario resumenDiario(LocalDate fecha);

    record ResumenDiario(
        LocalDate fecha,
        int total,
        Map<String, Long> porEstado,
        Map<String, Long> porSede,
        Map<String, Long> porEspecialidad,
        List<CitaNotificable> citas
    ) {}
}
