package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Mapea `appointment_status_history` (HU-023, RN-11/RN-12). Solo se expone
 * inserción/lectura desde el adaptador: no hay ningún método de
 * actualización/borrado (la auditoría es inmutable).
 */
@Entity
@Table(name = "appointment_status_history")
public class AppointmentStatusHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private AppointmentStatusJpaEntity status;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @Column(name = "change_source", nullable = false, length = 20)
    private String changeSource;

    @Column(length = 500)
    private String reason;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    protected AppointmentStatusHistoryJpaEntity() {
        // JPA
    }

    public AppointmentStatusHistoryJpaEntity(Long appointmentId, AppointmentStatusJpaEntity status,
                                              Long changedByUserId, String changeSource, String reason,
                                              LocalDateTime changedAt) {
        this.appointmentId = appointmentId;
        this.status = status;
        this.changedByUserId = changedByUserId;
        this.changeSource = changeSource;
        this.reason = reason;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public AppointmentStatusJpaEntity getStatus() {
        return status;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public String getChangeSource() {
        return changeSource;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
