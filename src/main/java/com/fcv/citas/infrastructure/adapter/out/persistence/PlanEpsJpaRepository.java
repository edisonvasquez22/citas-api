package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanEpsJpaRepository extends JpaRepository<PlanEpsJpaEntity, Long> {

    boolean existsByEpsIdAndCode(Long epsId, String code);

    List<PlanEpsJpaEntity> findByEpsId(Long epsId);

    List<PlanEpsJpaEntity> findByEpsIdAndActiveTrue(Long epsId);
}
