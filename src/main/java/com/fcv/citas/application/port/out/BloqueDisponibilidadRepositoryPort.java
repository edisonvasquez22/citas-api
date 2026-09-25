package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.BloqueDisponibilidad;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para bloques de disponibilidad y su discretización en
 * slots de 30 minutos (HU-012). Guardar/actualizar un bloque también crea o
 * regenera sus `professional_slots` (responsabilidad del adaptador, usando
 * {@code BloqueDisponibilidad.generarSlots()}).
 */
public interface BloqueDisponibilidadRepositoryPort {

    /** RN-06: solapamiento entre bloques del mismo profesional el mismo día. */
    boolean existeSolapamiento(Long profesionalId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                Long excluirBloqueId);

    BloqueDisponibilidad guardar(BloqueDisponibilidad bloque);

    Optional<BloqueDisponibilidad> buscarPorId(Long id);

    List<BloqueDisponibilidad> listarPorProfesional(Long profesionalId);

    /** CA-05: un bloque con al menos un slot ya reservado por una cita no se puede editar/eliminar. */
    boolean tieneSlotsComprometidos(Long bloqueId);

    /** Reemplaza horario/sede y regenera los slots del bloque; solo si {@code tieneSlotsComprometidos} es falso. */
    BloqueDisponibilidad actualizarHorario(BloqueDisponibilidad bloqueActualizado);

    /** Solo si {@code tieneSlotsComprometidos} es falso. */
    void eliminar(Long bloqueId);
}
