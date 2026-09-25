package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Fila de `professional_locations` (HU-010). */
@Embeddable
public class SedeAsignadaEmbeddable {

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected SedeAsignadaEmbeddable() {
        // JPA
    }

    public SedeAsignadaEmbeddable(Long locationId, boolean active) {
        this.locationId = locationId;
        this.active = active;
    }

    public Long getLocationId() {
        return locationId;
    }

    public boolean isActive() {
        return active;
    }
}
