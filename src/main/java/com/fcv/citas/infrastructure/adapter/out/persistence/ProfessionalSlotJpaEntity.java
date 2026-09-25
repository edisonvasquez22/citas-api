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
 * Mapea `professional_slots` (HU-012/HU-013/HU-014/HU-015). {@code appointmentId}
 * nulo significa libre; la reserva atómica (RN-01) vive en
 * {@code ProfessionalSlotJpaRepository.reservarAtomicamente} como un
 * `UPDATE ... WHERE appointment_id IS NULL`.
 */
@Entity
@Table(name = "professional_slots")
public class ProfessionalSlotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_block_id", nullable = false)
    private AvailabilityBlockJpaEntity availabilityBlock;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "appointment_id")
    private Long appointmentId;

    protected ProfessionalSlotJpaEntity() {
        // JPA
    }

    public ProfessionalSlotJpaEntity(AvailabilityBlockJpaEntity availabilityBlock, LocalDateTime startAt,
                                      LocalDateTime endAt) {
        this.availabilityBlock = availabilityBlock;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public Long getId() {
        return id;
    }

    public AvailabilityBlockJpaEntity getAvailabilityBlock() {
        return availabilityBlock;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }
}
