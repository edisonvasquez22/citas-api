package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.FuenteCambioEstado;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `appointment_status_history` (HU-023). */
@Component
@Profile("!test")
public class HistorialEstadoCitaJpaAdapter implements HistorialEstadoCitaPort {

    private final AppointmentStatusHistoryJpaRepository historyJpaRepository;
    private final AppointmentStatusJpaRepository statusJpaRepository;

    public HistorialEstadoCitaJpaAdapter(AppointmentStatusHistoryJpaRepository historyJpaRepository,
                                          AppointmentStatusJpaRepository statusJpaRepository) {
        this.historyJpaRepository = historyJpaRepository;
        this.statusJpaRepository = statusJpaRepository;
    }

    @Override
    @Transactional
    public void registrar(TransicionEstadoCita transicion) {
        AppointmentStatusJpaEntity status = statusJpaRepository.findByCode(transicion.estado().name())
            .orElseThrow(() -> new IllegalStateException(
                "Estado de cita no encontrado en el catálogo: " + transicion.estado()));
        historyJpaRepository.save(new AppointmentStatusHistoryJpaEntity(transicion.citaId(), status,
            transicion.actorUsuarioId(), transicion.fuente().name(), transicion.motivo(), transicion.momento()));
    }

    @Override
    public List<TransicionEstadoCita> listarPorCita(Long citaId) {
        return historyJpaRepository.findByAppointmentIdOrderByChangedAtAsc(citaId).stream()
            .map(entidad -> new TransicionEstadoCita(entidad.getId(), entidad.getAppointmentId(),
                EstadoCita.valueOf(entidad.getStatus().getCode()), entidad.getChangedByUserId(),
                FuenteCambioEstado.valueOf(entidad.getChangeSource()), entidad.getReason(), entidad.getChangedAt()))
            .toList();
    }
}
