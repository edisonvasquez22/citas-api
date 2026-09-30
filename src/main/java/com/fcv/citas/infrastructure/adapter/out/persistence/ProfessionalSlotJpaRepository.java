package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfessionalSlotJpaRepository extends JpaRepository<ProfessionalSlotJpaEntity, Long> {

    List<ProfessionalSlotJpaEntity> findByAvailabilityBlockId(Long availabilityBlockId);

    void deleteByAvailabilityBlockId(Long availabilityBlockId);

    boolean existsByAvailabilityBlockIdAndAppointmentIdIsNotNull(Long availabilityBlockId);

    /** HU-013: slots libres para un conjunto de profesionales candidatos, opcionalmente filtrados por sede. */
    // JOIN FETCH: SlotJpaAdapter mapea el bloque fuera de la transacción; sin él, el proxy LAZY lanza
    // LazyInitializationException (open-in-view está desactivado).
    @Query("SELECT s FROM ProfessionalSlotJpaEntity s JOIN FETCH s.availabilityBlock b WHERE s.appointmentId IS NULL "
        + "AND b.availableDate = :fecha "
        + "AND b.professionalId IN :profesionalIds "
        + "AND (:sedeId IS NULL OR b.locationId = :sedeId) "
        + "ORDER BY b.professionalId, s.startAt")
    List<ProfessionalSlotJpaEntity> buscarLibres(@Param("profesionalIds") Set<Long> profesionalIds,
                                                  @Param("sedeId") Long sedeId, @Param("fecha") LocalDate fecha);

    /** HU-014/HU-015: candidatos ordenados desde {@code inicio}, para validar contigüidad en el adaptador. */
    @Query("SELECT s FROM ProfessionalSlotJpaEntity s WHERE s.availabilityBlock.professionalId = :profesionalId "
        + "AND s.availabilityBlock.locationId = :sedeId AND s.startAt >= :inicio ORDER BY s.startAt")
    List<ProfessionalSlotJpaEntity> buscarDesde(@Param("profesionalId") Long profesionalId,
                                                 @Param("sedeId") Long sedeId, @Param("inicio") LocalDateTime inicio,
                                                 Pageable pageable);

    /**
     * RN-01: reserva atómica. El `UPDATE ... WHERE appointment_id IS NULL` toma
     * bloqueo de fila en MySQL/InnoDB, así que bajo concurrencia solo una
     * transacción puede ganar cada slot (ver SolicitarCitaGeneralServiceTest).
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProfessionalSlotJpaEntity s SET s.appointmentId = :citaId "
        + "WHERE s.id IN :slotIds AND s.appointmentId IS NULL")
    int reservarAtomicamente(@Param("slotIds") List<Long> slotIds, @Param("citaId") Long citaId);

    /** RN-09 / compensación de reserva parcial fallida. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProfessionalSlotJpaEntity s SET s.appointmentId = NULL WHERE s.appointmentId = :citaId")
    void liberarSlotsDeCita(@Param("citaId") Long citaId);

    /** HU-020: distingue franja antigua vs nueva de una cita en reprogramación por su horario. */
    @Query("SELECT s.id FROM ProfessionalSlotJpaEntity s WHERE s.appointmentId = :citaId "
        + "AND s.startAt >= :inicio AND s.startAt < :fin")
    List<Long> listarIdsDeCitaEnRango(@Param("citaId") Long citaId, @Param("inicio") LocalDateTime inicio,
                                       @Param("fin") LocalDateTime fin);

    /** HU-020: libera puntualmente los slots indicados. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProfessionalSlotJpaEntity s SET s.appointmentId = NULL WHERE s.id IN :slotIds")
    void liberarSlots(@Param("slotIds") List<Long> slotIds);
}
