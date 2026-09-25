package com.fcv.citas.domain.model;

/** Catálogo fijo `appointment_statuses` (V2). RF-11/RF-12/RF-19. */
public enum EstadoCita {
    REQUESTED,
    APPROVED,
    REJECTED,
    CANCELLED,
    COMPLETED,
    NO_SHOW
}
