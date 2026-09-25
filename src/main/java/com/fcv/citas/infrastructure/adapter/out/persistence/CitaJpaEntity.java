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
 * Mapea `appointments` (HU-014/HU-015/HU-016). No existe columna propia para
 * el motivo de rechazo: ese motivo se audita en
 * `appointment_status_history.reason` (ver HU-023), no se duplica aquí.
 */
@Entity
@Table(name = "appointments")
public class CitaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    @Column(name = "professional_id", nullable = false)
    private Long professionalId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "specialty_id", nullable = false)
    private Long specialtyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private AppointmentStatusJpaEntity status;

    @Column(length = 500)
    private String reason;

    @Column(name = "scheduled_start_at", nullable = false)
    private LocalDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    private LocalDateTime scheduledEndAt;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    protected CitaJpaEntity() {
        // JPA
    }

    public CitaJpaEntity(Long id, Long patientUserId, Long professionalId, Long locationId, Long specialtyId,
                          AppointmentStatusJpaEntity status, String reason, LocalDateTime scheduledStartAt,
                          LocalDateTime scheduledEndAt, Long createdByUserId, Long approvedByUserId,
                          LocalDateTime approvedAt) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.professionalId = professionalId;
        this.locationId = locationId;
        this.specialtyId = specialtyId;
        this.status = status;
        this.reason = reason;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.createdByUserId = createdByUserId;
        this.approvedByUserId = approvedByUserId;
        this.approvedAt = approvedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public AppointmentStatusJpaEntity getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getScheduledStartAt() {
        return scheduledStartAt;
    }

    public LocalDateTime getScheduledEndAt() {
        return scheduledEndAt;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public Long getApprovedByUserId() {
        return approvedByUserId;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
}
