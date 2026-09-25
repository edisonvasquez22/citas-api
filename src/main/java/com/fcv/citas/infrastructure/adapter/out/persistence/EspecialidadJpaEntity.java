package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Mapea `specialties` (HU-009, RF-06/RF-09). */
@Entity
@Table(name = "specialties")
public class EspecialidadJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "appointment_duration_minutes", nullable = false)
    private int appointmentDurationMinutes;

    @Column(name = "is_general", nullable = false)
    private boolean general;

    @Column(name = "requires_admin_approval", nullable = false)
    private boolean requiresAdminApproval;

    @Column(nullable = false)
    private boolean active = true;

    protected EspecialidadJpaEntity() {
        // JPA
    }

    public EspecialidadJpaEntity(Long id, String code, String name, int appointmentDurationMinutes,
                                  boolean general, boolean requiresAdminApproval, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.appointmentDurationMinutes = appointmentDurationMinutes;
        this.general = general;
        this.requiresAdminApproval = requiresAdminApproval;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getAppointmentDurationMinutes() {
        return appointmentDurationMinutes;
    }

    public boolean isGeneral() {
        return general;
    }

    public boolean isRequiresAdminApproval() {
        return requiresAdminApproval;
    }

    public boolean isActive() {
        return active;
    }
}
