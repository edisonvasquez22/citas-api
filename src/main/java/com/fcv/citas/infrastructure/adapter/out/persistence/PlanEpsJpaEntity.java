package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Mapea `eps_plans` (HU-008, RF-06). Cada plan pertenece a una EPS y a un régimen fijo (RF-05). */
@Entity
@Table(name = "eps_plans")
public class PlanEpsJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "eps_id", nullable = false)
    private Long epsId;

    @Column(name = "regime_id", nullable = false)
    private Long regimeId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    protected PlanEpsJpaEntity() {
        // JPA
    }

    public PlanEpsJpaEntity(Long id, Long epsId, Long regimeId, String code, String name, boolean active) {
        this.id = id;
        this.epsId = epsId;
        this.regimeId = regimeId;
        this.code = code;
        this.name = name;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Long getEpsId() {
        return epsId;
    }

    public Long getRegimeId() {
        return regimeId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
