package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Solo `save`/lecturas (heredadas de JpaRepository): la auditoría no se edita ni se borra (RN-12). */
public interface AppointmentStatusHistoryJpaRepository extends JpaRepository<AppointmentStatusHistoryJpaEntity, Long> {

    List<AppointmentStatusHistoryJpaEntity> findByAppointmentIdOrderByChangedAtAsc(Long appointmentId);
}
