package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RescheduleRequestStatusJpaRepository extends JpaRepository<RescheduleRequestStatusJpaEntity, Long> {

    Optional<RescheduleRequestStatusJpaEntity> findByCode(String code);
}
