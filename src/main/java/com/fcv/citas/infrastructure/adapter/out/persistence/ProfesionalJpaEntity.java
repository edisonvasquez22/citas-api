package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/** Mapea `professionals` + `professional_specialties` + `professional_locations` (HU-010/HU-011). */
@Entity
@Table(name = "professionals")
public class ProfesionalJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "professional_code", nullable = false, unique = true, length = 40)
    private String professionalCode;

    @Column(name = "license_number", nullable = false, unique = true, length = 80)
    private String licenseNumber;

    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "professional_specialties", joinColumns = @JoinColumn(name = "professional_id"))
    private Set<EspecialidadAsignadaEmbeddable> especialidades = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "professional_locations", joinColumns = @JoinColumn(name = "professional_id"))
    private Set<SedeAsignadaEmbeddable> sedes = new HashSet<>();

    protected ProfesionalJpaEntity() {
        // JPA
    }

    public ProfesionalJpaEntity(Long id, Long userId, String professionalCode, String licenseNumber, boolean active,
                                 Set<EspecialidadAsignadaEmbeddable> especialidades, Set<SedeAsignadaEmbeddable> sedes) {
        this.id = id;
        this.userId = userId;
        this.professionalCode = professionalCode;
        this.licenseNumber = licenseNumber;
        this.active = active;
        this.especialidades = especialidades;
        this.sedes = sedes;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getProfessionalCode() {
        return professionalCode;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public boolean isActive() {
        return active;
    }

    public Set<EspecialidadAsignadaEmbeddable> getEspecialidades() {
        return especialidades;
    }

    public Set<SedeAsignadaEmbeddable> getSedes() {
        return sedes;
    }
}
