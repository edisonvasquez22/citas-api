package com.fcv.citas.domain.model;

import com.fcv.citas.domain.exception.ValidacionNegocioException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Agregado de dominio para HU-012 (RF-08, RN-06). Sin dependencias de
 * Spring/JPA. El solapamiento contra otros bloques del mismo profesional y la
 * habilitación en la sede se validan en el caso de uso (requieren consultar
 * repositorios); aquí solo viven las reglas verificables con los datos del
 * propio bloque.
 */
public final class BloqueDisponibilidad {

    public static final int DURACION_SLOT_MINUTOS = 30;

    private final Long id;
    private final Long profesionalId;
    private final Long sedeId;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final boolean activo;

    private BloqueDisponibilidad(Long id, Long profesionalId, Long sedeId, LocalDate fecha, LocalTime horaInicio,
                                  LocalTime horaFin, boolean activo) {
        this.id = id;
        this.profesionalId = profesionalId;
        this.sedeId = sedeId;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.activo = activo;
    }

    /** CA-01/CA-02: rechaza bloques en el pasado o cuya duración no discretiza en slots de 30 min. */
    public static BloqueDisponibilidad crear(Long profesionalId, Long sedeId, LocalDate fecha, LocalTime horaInicio,
                                              LocalTime horaFin, LocalDateTime ahora) {
        if (profesionalId == null || sedeId == null) {
            throw new IllegalArgumentException("profesionalId y sedeId no pueden ser nulos");
        }
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("fecha, horaInicio y horaFin son obligatorios");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new ValidacionNegocioException("horaFin debe ser posterior a horaInicio");
        }
        long minutos = Duration.between(horaInicio, horaFin).toMinutes();
        if (minutos % DURACION_SLOT_MINUTOS != 0) {
            throw new ValidacionNegocioException(
                "La duración del bloque debe ser múltiplo de " + DURACION_SLOT_MINUTOS + " minutos");
        }
        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        if (!inicio.isAfter(ahora)) {
            throw new ValidacionNegocioException("No se pueden crear bloques de disponibilidad en el pasado");
        }
        return new BloqueDisponibilidad(null, profesionalId, sedeId, fecha, horaInicio, horaFin, true);
    }

    public static BloqueDisponibilidad reconstruir(Long id, Long profesionalId, Long sedeId, LocalDate fecha,
                                                     LocalTime horaInicio, LocalTime horaFin, boolean activo) {
        return new BloqueDisponibilidad(id, profesionalId, sedeId, fecha, horaInicio, horaFin, activo);
    }

    /** RN-06: dos bloques del mismo profesional se solapan si sus rangos [inicio,fin) se cruzan el mismo día. */
    public boolean seSolapaCon(BloqueDisponibilidad otro) {
        if (!this.fecha.equals(otro.fecha)) {
            return false;
        }
        return this.horaInicio.isBefore(otro.horaFin) && otro.horaInicio.isBefore(this.horaFin);
    }

    /** Discretiza el bloque en slots atómicos de 30 minutos (aún no persistidos: bloqueId/id quedan null). */
    public List<RangoSlot> generarSlots() {
        List<RangoSlot> slots = new ArrayList<>();
        LocalDateTime cursor = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);
        while (cursor.isBefore(fin)) {
            LocalDateTime siguiente = cursor.plusMinutes(DURACION_SLOT_MINUTOS);
            slots.add(new RangoSlot(cursor, siguiente));
            cursor = siguiente;
        }
        return slots;
    }

    public record RangoSlot(LocalDateTime inicio, LocalDateTime fin) {}

    public Long getId() {
        return id;
    }

    public Long getProfesionalId() {
        return profesionalId;
    }

    public Long getSedeId() {
        return sedeId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public boolean isActivo() {
        return activo;
    }
}
