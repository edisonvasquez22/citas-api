package com.fcv.citas.domain.model;

import com.fcv.citas.domain.exception.TransicionEstadoInvalidaException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import java.time.LocalDateTime;

/**
 * Agregado de dominio para HU-019/HU-020 (RF-15, RN-01, RN-04, RN-09, RN-10).
 * Sin dependencias de Spring/JPA. Modela solo el ciclo de vida de la solicitud
 * de reprogramación; la cita original ({@link Cita}) no se toca hasta que
 * ADMIN aprueba (RN-10) — ver {@code GestionarReprogramacionesService}.
 */
public final class SolicitudReprogramacion {

    private final Long id;
    private final Long citaId;
    private final Long solicitadoPorUsuarioId;
    private final Long sedeSolicitadaId;
    private final EstadoSolicitudReprogramacion estado;
    private final LocalDateTime inicioAnterior;
    private final LocalDateTime finAnterior;
    private final LocalDateTime inicioSolicitado;
    private final LocalDateTime finSolicitado;
    private final String motivoDecision;
    private final Long decididoPorUsuarioId;
    private final LocalDateTime decididoEn;

    private SolicitudReprogramacion(Long id, Long citaId, Long solicitadoPorUsuarioId, Long sedeSolicitadaId,
                                     EstadoSolicitudReprogramacion estado, LocalDateTime inicioAnterior,
                                     LocalDateTime finAnterior, LocalDateTime inicioSolicitado,
                                     LocalDateTime finSolicitado, String motivoDecision, Long decididoPorUsuarioId,
                                     LocalDateTime decididoEn) {
        this.id = id;
        this.citaId = citaId;
        this.solicitadoPorUsuarioId = solicitadoPorUsuarioId;
        this.sedeSolicitadaId = sedeSolicitadaId;
        this.estado = estado;
        this.inicioAnterior = inicioAnterior;
        this.finAnterior = finAnterior;
        this.inicioSolicitado = inicioSolicitado;
        this.finSolicitado = finSolicitado;
        this.motivoDecision = motivoDecision;
        this.decididoPorUsuarioId = decididoPorUsuarioId;
        this.decididoEn = decididoEn;
    }

    /** HU-019 CA-01: nace en PENDING; RN-10, la cita original conserva su franja hasta la decisión. */
    public static SolicitudReprogramacion solicitar(Long citaId, Long solicitadoPorUsuarioId, Long sedeSolicitadaId,
                                                      LocalDateTime inicioAnterior, LocalDateTime finAnterior,
                                                      LocalDateTime inicioSolicitado, LocalDateTime finSolicitado) {
        if (citaId == null || solicitadoPorUsuarioId == null || sedeSolicitadaId == null) {
            throw new IllegalArgumentException("citaId, solicitadoPorUsuarioId y sedeSolicitadaId son obligatorios");
        }
        if (inicioSolicitado == null || finSolicitado == null || !finSolicitado.isAfter(inicioSolicitado)) {
            throw new ValidacionNegocioException("El horario solicitado no es válido");
        }
        return new SolicitudReprogramacion(null, citaId, solicitadoPorUsuarioId, sedeSolicitadaId,
            EstadoSolicitudReprogramacion.PENDING, inicioAnterior, finAnterior, inicioSolicitado, finSolicitado, null,
            null, null);
    }

    public static SolicitudReprogramacion reconstruir(Long id, Long citaId, Long solicitadoPorUsuarioId,
                                                        Long sedeSolicitadaId, EstadoSolicitudReprogramacion estado,
                                                        LocalDateTime inicioAnterior, LocalDateTime finAnterior,
                                                        LocalDateTime inicioSolicitado, LocalDateTime finSolicitado,
                                                        String motivoDecision, Long decididoPorUsuarioId,
                                                        LocalDateTime decididoEn) {
        return new SolicitudReprogramacion(id, citaId, solicitadoPorUsuarioId, sedeSolicitadaId, estado,
            inicioAnterior, finAnterior, inicioSolicitado, finSolicitado, motivoDecision, decididoPorUsuarioId,
            decididoEn);
    }

    /** HU-020 CA-01: solo se puede aprobar una solicitud que sigue en PENDING. */
    public SolicitudReprogramacion aprobar(Long adminUsuarioId, LocalDateTime ahora) {
        requerirPendiente();
        return new SolicitudReprogramacion(id, citaId, solicitadoPorUsuarioId, sedeSolicitadaId,
            EstadoSolicitudReprogramacion.APPROVED, inicioAnterior, finAnterior, inicioSolicitado, finSolicitado,
            null, adminUsuarioId, ahora);
    }

    /** HU-020 CA-02/CA-03: el rechazo exige motivo y solo aplica sobre una solicitud en PENDING. */
    public SolicitudReprogramacion rechazar(Long adminUsuarioId, String motivoRechazo, LocalDateTime ahora) {
        requerirPendiente();
        if (motivoRechazo == null || motivoRechazo.isBlank()) {
            throw new ValidacionNegocioException("El rechazo de una reprogramación requiere un motivo");
        }
        return new SolicitudReprogramacion(id, citaId, solicitadoPorUsuarioId, sedeSolicitadaId,
            EstadoSolicitudReprogramacion.REJECTED, inicioAnterior, finAnterior, inicioSolicitado, finSolicitado,
            motivoRechazo, adminUsuarioId, ahora);
    }

    private void requerirPendiente() {
        if (estado != EstadoSolicitudReprogramacion.PENDING) {
            throw new TransicionEstadoInvalidaException(
                "La solicitud debe estar en estado PENDING para esta operación (estado actual: " + estado + ")");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCitaId() {
        return citaId;
    }

    public Long getSolicitadoPorUsuarioId() {
        return solicitadoPorUsuarioId;
    }

    public Long getSedeSolicitadaId() {
        return sedeSolicitadaId;
    }

    public EstadoSolicitudReprogramacion getEstado() {
        return estado;
    }

    public LocalDateTime getInicioAnterior() {
        return inicioAnterior;
    }

    public LocalDateTime getFinAnterior() {
        return finAnterior;
    }

    public LocalDateTime getInicioSolicitado() {
        return inicioSolicitado;
    }

    public LocalDateTime getFinSolicitado() {
        return finSolicitado;
    }

    public String getMotivoDecision() {
        return motivoDecision;
    }

    public Long getDecididoPorUsuarioId() {
        return decididoPorUsuarioId;
    }

    public LocalDateTime getDecididoEn() {
        return decididoEn;
    }
}
