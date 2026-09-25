package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfesionalJpaRepository extends JpaRepository<ProfesionalJpaEntity, Long> {

    boolean existsByProfessionalCode(String professionalCode);

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<ProfesionalJpaEntity> findByUserId(Long userId);

    List<ProfesionalJpaEntity> findByActiveTrue();
}
