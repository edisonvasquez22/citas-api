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

/** Mapea `reschedule_requests` (HU-019/HU-020). RN-10: la cita original no se toca mientras está PENDING. */
@Entity
@Table(name = "reschedule_requests")
public class RescheduleRequestJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "requested_by_user_id", nullable = false)
    private Long requestedByUserId;

    @Column(name = "requested_location_id", nullable = false)
    private Long requestedLocationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private RescheduleRequestStatusJpaEntity status;

    @Column(name = "previous_start_at", nullable = false)
    private LocalDateTime previousStartAt;

    @Column(name = "previous_end_at", nullable = false)
    private LocalDateTime previousEndAt;

    @Column(name = "requested_start_at", nullable = false)
    private LocalDateTime requestedStartAt;

    @Column(name = "requested_end_at", nullable = false)
    private LocalDateTime requestedEndAt;

    @Column(name = "decision_reason", length = 500)
    private String decisionReason;

    @Column(name = "decided_by_user_id")
    private Long decidedByUserId;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    protected RescheduleRequestJpaEntity() {
        // JPA
    }

    public RescheduleRequestJpaEntity(Long id, Long appointmentId, Long requestedByUserId, Long requestedLocationId,
                                       RescheduleRequestStatusJpaEntity status, LocalDateTime previousStartAt,
                                       LocalDateTime previousEndAt, LocalDateTime requestedStartAt,
                                       LocalDateTime requestedEndAt, String decisionReason, Long decidedByUserId,
                                       LocalDateTime decidedAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.requestedByUserId = requestedByUserId;
        this.requestedLocationId = requestedLocationId;
        this.status = status;
        this.previousStartAt = previousStartAt;
        this.previousEndAt = previousEndAt;
        this.requestedStartAt = requestedStartAt;
        this.requestedEndAt = requestedEndAt;
        this.decisionReason = decisionReason;
        this.decidedByUserId = decidedByUserId;
        this.decidedAt = decidedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public Long getRequestedByUserId() {
        return requestedByUserId;
    }

    public Long getRequestedLocationId() {
        return requestedLocationId;
    }

    public RescheduleRequestStatusJpaEntity getStatus() {
        return status;
    }

    public LocalDateTime getPreviousStartAt() {
        return previousStartAt;
    }

    public LocalDateTime getPreviousEndAt() {
        return previousEndAt;
    }

    public LocalDateTime getRequestedStartAt() {
        return requestedStartAt;
    }

    public LocalDateTime getRequestedEndAt() {
        return requestedEndAt;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public Long getDecidedByUserId() {
        return decidedByUserId;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }
}
