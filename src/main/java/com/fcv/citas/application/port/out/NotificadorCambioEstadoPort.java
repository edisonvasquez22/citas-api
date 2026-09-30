package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.EventoCambioEstado;

/** WF-002: la implementación no debe propagar fallos al caso de uso (la notificación es best-effort). */
public interface NotificadorCambioEstadoPort {

    void notificar(EventoCambioEstado evento);
}
