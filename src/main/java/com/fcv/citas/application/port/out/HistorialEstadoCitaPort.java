package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.TransicionEstadoCita;
import java.util.List;

/**
 * Puerto de salida para HU-023. Deliberadamente sin ningún método de edición
 * o borrado (RN-12: la auditoría es inmutable).
 */
public interface HistorialEstadoCitaPort {

    void registrar(TransicionEstadoCita transicion);

    List<TransicionEstadoCita> listarPorCita(Long citaId);
}
