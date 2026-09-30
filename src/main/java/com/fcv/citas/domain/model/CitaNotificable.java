package com.fcv.citas.domain.model;

import java.time.LocalDateTime;

/** Vista de lectura de una cita con los datos de contacto que necesitan las automatizaciones (n8n). */
public record CitaNotificable(
    Long citaId,
    String estado,
    LocalDateTime inicio,
    LocalDateTime fin,
    Long pacienteUsuarioId,
    String pacienteNombre,
    String pacienteEmail,
    String profesionalNombre,
    String sedeCodigo,
    String sedeNombre,
    String especialidadNombre
) {}
