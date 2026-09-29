package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaJpaRepository extends JpaRepository<CitaJpaEntity, Long> {

    /** HU-016 CA-04: bandeja filtrable. {@code desde}/{@code hasta} acotan el día (ambos nulos = sin filtro de fecha). */
    @Query("SELECT c FROM CitaJpaEntity c WHERE c.status.code = :statusCode "
        + "AND (:sedeId IS NULL OR c.locationId = :sedeId) "
        + "AND (:profesionalId IS NULL OR c.professionalId = :profesionalId) "
        + "AND (:especialidadId IS NULL OR c.specialtyId = :especialidadId) "
        + "AND (:desde IS NULL OR c.scheduledStartAt >= :desde) "
        + "AND (:hasta IS NULL OR c.scheduledStartAt < :hasta) "
        + "ORDER BY c.scheduledStartAt")
    List<CitaJpaEntity> listarPorEstado(@Param("statusCode") String statusCode, @Param("sedeId") Long sedeId,
                                         @Param("profesionalId") Long profesionalId,
                                         @Param("especialidadId") Long especialidadId,
                                         @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    /** HU-017: citas propias del paciente. {@code statusCode}/{@code desde}/{@code hasta} opcionales. */
    @Query("SELECT c FROM CitaJpaEntity c WHERE c.patientUserId = :pacienteUsuarioId "
        + "AND (:statusCode IS NULL OR c.status.code = :statusCode) "
        + "AND (:desde IS NULL OR c.scheduledStartAt >= :desde) "
        + "AND (:hasta IS NULL OR c.scheduledStartAt < :hasta) "
        + "ORDER BY c.scheduledStartAt DESC")
    List<CitaJpaEntity> listarPorPaciente(@Param("pacienteUsuarioId") Long pacienteUsuarioId,
                                           @Param("statusCode") String statusCode,
                                           @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    /** HU-021: agenda propia del profesional. {@code statusCode}/{@code sedeId}/{@code desde}/{@code hasta} opcionales. */
    @Query("SELECT c FROM CitaJpaEntity c WHERE c.professionalId = :profesionalId "
        + "AND (:statusCode IS NULL OR c.status.code = :statusCode) "
        + "AND (:sedeId IS NULL OR c.locationId = :sedeId) "
        + "AND (:desde IS NULL OR c.scheduledStartAt >= :desde) "
        + "AND (:hasta IS NULL OR c.scheduledStartAt < :hasta) "
        + "ORDER BY c.scheduledStartAt")
    List<CitaJpaEntity> listarPorProfesional(@Param("profesionalId") Long profesionalId,
                                              @Param("statusCode") String statusCode, @Param("sedeId") Long sedeId,
                                              @Param("desde") LocalDateTime desde,
                                              @Param("hasta") LocalDateTime hasta);
}
