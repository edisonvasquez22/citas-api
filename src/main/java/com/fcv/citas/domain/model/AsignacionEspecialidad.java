package com.fcv.citas.domain.model;

/** Especialidad asignada a un profesional (tabla `professional_specialties`). HU-010. */
public record AsignacionEspecialidad(Long especialidadId, boolean primaria) {

    public AsignacionEspecialidad {
        if (especialidadId == null) {
            throw new IllegalArgumentException("especialidadId no puede ser nulo");
        }
    }
}
