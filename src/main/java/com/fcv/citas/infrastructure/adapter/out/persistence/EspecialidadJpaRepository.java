package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadJpaRepository extends JpaRepository<EspecialidadJpaEntity, Long> {

    boolean existsByCode(String code);

    List<EspecialidadJpaEntity> findByActiveTrue();
}
