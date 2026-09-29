package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.EstadoSolicitudReprogramacion;
import com.fcv.citas.domain.model.SolicitudReprogramacion;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para solicitudes de reprogramación (HU-019/HU-020), sobre `reschedule_requests`. */
public interface SolicitudReprogramacionRepositoryPort {

    SolicitudReprogramacion guardar(SolicitudReprogramacion solicitud);

    Optional<SolicitudReprogramacion> buscarPorId(Long id);

    /** HU-020: bandeja de solicitudes en un estado dado (típicamente PENDING). */
    List<SolicitudReprogramacion> listarPorEstado(EstadoSolicitudReprogramacion estado);

    /**
     * HU-017: la solicitud de reprogramación más reciente de una cita (cualquiera sea su estado), para que el
     * propio paciente pueda ver el resultado de su solicitud en `GET /api/appointments/mine` — el estado de la
     * cita misma no cambia mientras la reprogramación está PENDING (RN-10), así que sin esto el paciente no
     * tenía forma de conocer el desenlace de su solicitud una vez decidida.
     */
    Optional<SolicitudReprogramacion> buscarUltimaPorCita(Long citaId);
}
