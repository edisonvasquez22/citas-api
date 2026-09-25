package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

/** Mapea `availability_blocks` (HU-012, RF-08). */
@Entity
@Table(name = "availability_blocks")
public class AvailabilityBlockJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professional_id", nullable = false)
    private Long professionalId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean active = true;

    protected AvailabilityBlockJpaEntity() {
        // JPA
    }

    public AvailabilityBlockJpaEntity(Long id, Long professionalId, Long locationId, LocalDate availableDate,
                                       LocalTime startTime, LocalTime endTime, boolean active) {
        this.id = id;
        this.professionalId = professionalId;
        this.locationId = locationId;
        this.availableDate = availableDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public LocalDate getAvailableDate() {
        return availableDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return active;
    }
}
