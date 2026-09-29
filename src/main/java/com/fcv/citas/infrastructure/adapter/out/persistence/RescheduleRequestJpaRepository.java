package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RescheduleRequestJpaRepository extends JpaRepository<RescheduleRequestJpaEntity, Long> {

    @Query("SELECT r FROM RescheduleRequestJpaEntity r WHERE r.status.code = :statusCode ORDER BY r.id")
    List<RescheduleRequestJpaEntity> listarPorEstado(@Param("statusCode") String statusCode);
}
