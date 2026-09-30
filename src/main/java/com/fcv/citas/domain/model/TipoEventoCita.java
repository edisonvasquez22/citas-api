package com.fcv.citas.domain.model;

/** Eventos que WF-002 (n8n) notifica al paciente. */
public enum TipoEventoCita {
    ESPECIALIZADA_APROBADA,
    ESPECIALIZADA_RECHAZADA,
    CITA_CANCELADA,
    REPROGRAMACION_APROBADA,
    REPROGRAMACION_RECHAZADA
}
