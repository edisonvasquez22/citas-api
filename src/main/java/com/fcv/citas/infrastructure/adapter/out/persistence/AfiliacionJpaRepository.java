package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfiliacionJpaRepository extends JpaRepository<AfiliacionJpaEntity, Long> {

    Optional<AfiliacionJpaEntity> findByUserIdAndCurrentTrue(Long userId);

    Optional<AfiliacionJpaEntity> findByUserIdAndPlanIdAndMembershipNumber(Long userId, Long planId,
                                                                            String membershipNumber);
}
