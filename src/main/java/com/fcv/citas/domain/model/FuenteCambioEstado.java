package com.fcv.citas.domain.model;

/** RF-19: quién origina una transición de estado de cita. Ver `appointment_status_history.change_source`. */
public enum FuenteCambioEstado {
    SYSTEM,
    USER,
    ADMIN
}
