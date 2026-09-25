package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationJpaRepository extends JpaRepository<LocationJpaEntity, Long> {

    List<LocationJpaEntity> findByActiveTrue();
}
