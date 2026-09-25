package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.ConsultarDisponibilidadUseCase;
import com.fcv.citas.infrastructure.adapter.in.web.dto.DisponibilidadDtos;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HU-013 — cualquier usuario autenticado consulta disponibilidad reservable. */
@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;

    public AvailabilityController(ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase) {
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
    }

    @GetMapping
    public List<DisponibilidadDtos.HorarioResponse> consultar(
            @RequestParam Long especialidadId,
            @RequestParam LocalDate fecha,
            @RequestParam(required = false) Long sedeId,
            @RequestParam(required = false) Long profesionalId) {
        return consultarDisponibilidadUseCase
            .consultar(new ConsultarDisponibilidadUseCase.Consulta(especialidadId, fecha, sedeId, profesionalId))
            .stream()
            .map(h -> new DisponibilidadDtos.HorarioResponse(h.profesionalId(), h.sedeId(), h.inicio(), h.fin()))
            .toList();
    }
}
