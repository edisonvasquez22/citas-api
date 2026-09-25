package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvailabilityBlockJpaRepository extends JpaRepository<AvailabilityBlockJpaEntity, Long> {

    List<AvailabilityBlockJpaEntity> findByProfessionalId(Long professionalId);

    /** RN-06: dos bloques del mismo profesional el mismo día se solapan si sus rangos [inicio,fin) se cruzan. */
    @Query("SELECT COUNT(b) > 0 FROM AvailabilityBlockJpaEntity b "
        + "WHERE b.professionalId = :profesionalId AND b.availableDate = :fecha "
        + "AND b.startTime < :horaFin AND b.endTime > :horaInicio "
        + "AND (:excluirId IS NULL OR b.id <> :excluirId)")
    boolean existeSolapamiento(@Param("profesionalId") Long profesionalId, @Param("fecha") LocalDate fecha,
                               @Param("horaInicio") LocalTime horaInicio, @Param("horaFin") LocalTime horaFin,
                               @Param("excluirId") Long excluirId);
}
