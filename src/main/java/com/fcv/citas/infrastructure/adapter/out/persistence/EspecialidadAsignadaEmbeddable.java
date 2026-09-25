package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Fila de `professional_specialties` (HU-010). */
@Embeddable
public class EspecialidadAsignadaEmbeddable {

    @Column(name = "specialty_id", nullable = false)
    private Long specialtyId;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected EspecialidadAsignadaEmbeddable() {
        // JPA
    }

    public EspecialidadAsignadaEmbeddable(Long specialtyId, boolean primary, boolean active) {
        this.specialtyId = specialtyId;
        this.primary = primary;
        this.active = active;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isActive() {
        return active;
    }
}
