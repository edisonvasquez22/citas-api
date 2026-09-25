package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentStatusJpaRepository extends JpaRepository<AppointmentStatusJpaEntity, Long> {

    Optional<AppointmentStatusJpaEntity> findByCode(String code);
}
