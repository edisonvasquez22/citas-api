package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para citas (HU-014/HU-015/HU-016). */
public interface CitaRepositoryPort {

    /** Inserta (id nulo) o actualiza (id presente) según corresponda; siempre devuelve la cita con id asignado. */
    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(Long id);

    /** HU-016 CA-04: bandeja filtrable de solicitudes en un estado dado (típicamente REQUESTED). */
    List<Cita> listarPorEstado(EstadoCita estado, Long sedeId, Long profesionalId, Long especialidadId,
                                LocalDate fecha);
}
