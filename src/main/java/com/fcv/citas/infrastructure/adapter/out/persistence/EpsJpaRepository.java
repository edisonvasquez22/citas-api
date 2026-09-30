package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpsJpaRepository extends JpaRepository<EpsJpaEntity, Long> {

    boolean existsByCode(String code);

    List<EpsJpaEntity> findByActiveTrue();
}
