package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.ConsultarCitasIntegracionUseCase;
import com.fcv.citas.application.port.in.ConsultarCitasIntegracionUseCase.ResumenDiario;
import com.fcv.citas.domain.model.CitaNotificable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de solo lectura para n8n, protegidos con la cabecera X-Integration-Key (no JWT). */
@RestController
@RequestMapping("/api/integration/appointments")
public class IntegrationController {

    private final ConsultarCitasIntegracionUseCase useCase;

    public IntegrationController(ConsultarCitasIntegracionUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/reminders")
    public List<CitaNotificable> recordatorios(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return useCase.citasParaRecordatorio(desde, hasta);
    }

    @GetMapping("/daily-summary")
    public ResumenDiario resumenDiario(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return useCase.resumenDiario(fecha);
    }
}
