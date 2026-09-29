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
}
