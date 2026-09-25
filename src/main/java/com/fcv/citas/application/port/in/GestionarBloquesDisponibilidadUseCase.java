package com.fcv.citas.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** HU-012 — Gestionar bloques de disponibilidad del profesional (RF-08, RN-06, RN-07). */
public interface GestionarBloquesDisponibilidadUseCase {

    Resultado crear(CrearCommand command);

    Resultado editar(EditarCommand command);

    void eliminar(Long usuarioId, Long bloqueId);

    List<Resultado> listarPropios(Long usuarioId);

    record CrearCommand(Long usuarioId, Long sedeId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {}

    record EditarCommand(Long usuarioId, Long bloqueId, Long sedeId, LocalDate fecha, LocalTime horaInicio,
                          LocalTime horaFin) {}

    record Resultado(Long id, Long profesionalId, Long sedeId, LocalDate fecha, LocalTime horaInicio,
                      LocalTime horaFin, boolean activo) {}
}
