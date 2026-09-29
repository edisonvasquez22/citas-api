package com.fcv.citas.domain.model;

import com.fcv.citas.domain.exception.TransicionEstadoInvalidaException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import java.time.LocalDateTime;

/**
 * Agregado de dominio para HU-014/HU-015/HU-016 (RF-11, RF-12, RN-01 a RN-04,
 * RN-09). Sin dependencias de Spring/JPA. La retención atómica del horario
 * (evitar doble reserva) vive en {@code SlotRepositoryPort}, no aquí: este
 * agregado modela solo el ciclo de vida de la cita una vez que el horario ya
 * quedó retenido.
 */
public final class Cita {

    private final Long id;
    private final Long pacienteUsuarioId;
    private final Long profesionalId;
    private final Long sedeId;
    private final Long especialidadId;
    private final EstadoCita estado;
    private final String motivo;
    private final LocalDateTime inicio;
    private final LocalDateTime fin;
    private final Long creadoPorUsuarioId;
    private final Long aprobadoPorUsuarioId;
    private final LocalDateTime aprobadoEn;
    private final String motivoDecision;

    private Cita(Long id, Long pacienteUsuarioId, Long profesionalId, Long sedeId, Long especialidadId,
                 EstadoCita estado, String motivo, LocalDateTime inicio, LocalDateTime fin,
                 Long creadoPorUsuarioId, Long aprobadoPorUsuarioId, LocalDateTime aprobadoEn,
                 String motivoDecision) {
        this.id = id;
        this.pacienteUsuarioId = pacienteUsuarioId;
        this.profesionalId = profesionalId;
        this.sedeId = sedeId;
        this.especialidadId = especialidadId;
        this.estado = estado;
        this.motivo = motivo;
        this.inicio = inicio;
        this.fin = fin;
        this.creadoPorUsuarioId = creadoPorUsuarioId;
        this.aprobadoPorUsuarioId = aprobadoPorUsuarioId;
        this.aprobadoEn = aprobadoEn;
        this.motivoDecision = motivoDecision;
    }

    /** HU-014 CA-01: cita general, aprobada automáticamente (RN-02), sin intervención de ADMIN. */
    public static Cita solicitarGeneral(Long pacienteUsuarioId, Long profesionalId, Long sedeId, Long especialidadId,
                                         String motivo, LocalDateTime inicio, LocalDateTime fin) {
        Cita cita = crearBase(pacienteUsuarioId, profesionalId, sedeId, especialidadId, motivo, inicio, fin);
        return new Cita(null, cita.pacienteUsuarioId, cita.profesionalId, cita.sedeId, cita.especialidadId,
            EstadoCita.APPROVED, cita.motivo, cita.inicio, cita.fin, pacienteUsuarioId, null, null, null);
    }

    /** HU-015 CA-01: cita especializada, nace en REQUESTED (RN-03: requiere decisión de ADMIN). */
    public static Cita solicitarEspecializada(Long pacienteUsuarioId, Long profesionalId, Long sedeId,
                                               Long especialidadId, String motivo, LocalDateTime inicio,
                                               LocalDateTime fin) {
        Cita cita = crearBase(pacienteUsuarioId, profesionalId, sedeId, especialidadId, motivo, inicio, fin);
        return new Cita(null, cita.pacienteUsuarioId, cita.profesionalId, cita.sedeId, cita.especialidadId,
            EstadoCita.REQUESTED, cita.motivo, cita.inicio, cita.fin, pacienteUsuarioId, null, null, null);
    }

    private static Cita crearBase(Long pacienteUsuarioId, Long profesionalId, Long sedeId, Long especialidadId,
                                   String motivo, LocalDateTime inicio, LocalDateTime fin) {
        if (pacienteUsuarioId == null || profesionalId == null || sedeId == null || especialidadId == null) {
            throw new IllegalArgumentException("paciente, profesional, sede y especialidad son obligatorios");
        }
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new ValidacionNegocioException("El horario de la cita no es válido");
        }
        return new Cita(null, pacienteUsuarioId, profesionalId, sedeId, especialidadId, null, motivo, inicio, fin,
            null, null, null, null);
    }

    public static Cita reconstruir(Long id, Long pacienteUsuarioId, Long profesionalId, Long sedeId,
                                    Long especialidadId, EstadoCita estado, String motivo, LocalDateTime inicio,
                                    LocalDateTime fin, Long creadoPorUsuarioId, Long aprobadoPorUsuarioId,
                                    LocalDateTime aprobadoEn, String motivoDecision) {
        return new Cita(id, pacienteUsuarioId, profesionalId, sedeId, especialidadId, estado, motivo, inicio, fin,
            creadoPorUsuarioId, aprobadoPorUsuarioId, aprobadoEn, motivoDecision);
    }

    /** HU-016 CA-01: solo se puede aprobar una solicitud que sigue en REQUESTED. */
    public Cita aprobar(Long adminUsuarioId, LocalDateTime ahora) {
        requerirEstado(EstadoCita.REQUESTED);
        return new Cita(id, pacienteUsuarioId, profesionalId, sedeId, especialidadId, EstadoCita.APPROVED, motivo,
            inicio, fin, creadoPorUsuarioId, adminUsuarioId, ahora, null);
    }

    /** HU-016 CA-02/CA-03: el rechazo exige motivo y solo aplica sobre una solicitud en REQUESTED. */
    public Cita rechazar(Long adminUsuarioId, String motivoRechazo, LocalDateTime ahora) {
        requerirEstado(EstadoCita.REQUESTED);
        if (motivoRechazo == null || motivoRechazo.isBlank()) {
            throw new ValidacionNegocioException("El rechazo de una cita especializada requiere un motivo");
        }
        return new Cita(id, pacienteUsuarioId, profesionalId, sedeId, especialidadId, EstadoCita.REJECTED, motivo,
            inicio, fin, creadoPorUsuarioId, adminUsuarioId, ahora, motivoRechazo);
    }

    /** HU-020 CA-01: al aprobar una reprogramación, la cita adopta el nuevo horario/sede sin cambiar de estado. */
    public Cita reprogramar(Long nuevaSedeId, LocalDateTime nuevoInicio, LocalDateTime nuevoFin) {
        requerirEstado(EstadoCita.APPROVED);
        return new Cita(id, pacienteUsuarioId, profesionalId, nuevaSedeId, especialidadId, estado, motivo,
            nuevoInicio, nuevoFin, creadoPorUsuarioId, aprobadoPorUsuarioId, aprobadoEn, motivoDecision);
    }

    /** HU-018 CA-01/CA-02: solo una cita futura y no terminal (APPROVED/REQUESTED) es cancelable. */
    public Cita cancelar(LocalDateTime ahora) {
        if (estado != EstadoCita.APPROVED && estado != EstadoCita.REQUESTED) {
            throw new TransicionEstadoInvalidaException(
                "Solo una cita APPROVED o REQUESTED puede cancelarse (estado actual: " + estado + ")");
        }
        if (!inicio.isAfter(ahora)) {
            throw new ValidacionNegocioException("Solo se pueden cancelar citas futuras");
        }
        return new Cita(id, pacienteUsuarioId, profesionalId, sedeId, especialidadId, EstadoCita.CANCELLED, motivo,
            inicio, fin, creadoPorUsuarioId, aprobadoPorUsuarioId, aprobadoEn, motivoDecision);
    }

    /** HU-022 CA-01: solo una cita APPROVED y ya pasada puede cerrarse como completada. */
    public Cita completar(LocalDateTime ahora) {
        requerirCerrable(ahora);
        return new Cita(id, pacienteUsuarioId, profesionalId, sedeId, especialidadId, EstadoCita.COMPLETED, motivo,
            inicio, fin, creadoPorUsuarioId, aprobadoPorUsuarioId, aprobadoEn, motivoDecision);
    }

    /** HU-022 CA-02: solo una cita APPROVED y ya pasada puede marcarse como no asistida. */
    public Cita marcarNoShow(LocalDateTime ahora) {
        requerirCerrable(ahora);
        return new Cita(id, pacienteUsuarioId, profesionalId, sedeId, especialidadId, EstadoCita.NO_SHOW, motivo,
            inicio, fin, creadoPorUsuarioId, aprobadoPorUsuarioId, aprobadoEn, motivoDecision);
    }

    private void requerirCerrable(LocalDateTime ahora) {
        requerirEstado(EstadoCita.APPROVED);
        if (fin.isAfter(ahora)) {
            throw new ValidacionNegocioException("Solo se puede cerrar una cita cuyo horario ya haya finalizado");
        }
    }

    private void requerirEstado(EstadoCita esperado) {
        if (estado != esperado) {
            throw new TransicionEstadoInvalidaException(
                "La cita debe estar en estado " + esperado + " para esta operación (estado actual: " + estado + ")");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteUsuarioId() {
        return pacienteUsuarioId;
    }

    public Long getProfesionalId() {
        return profesionalId;
    }

    public Long getSedeId() {
        return sedeId;
    }

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public Long getCreadoPorUsuarioId() {
        return creadoPorUsuarioId;
    }

    public Long getAprobadoPorUsuarioId() {
        return aprobadoPorUsuarioId;
    }

    public LocalDateTime getAprobadoEn() {
        return aprobadoEn;
    }

    public String getMotivoDecision() {
        return motivoDecision;
    }
}
