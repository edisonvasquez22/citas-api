package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.ConsultarMisCitasUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import java.time.LocalDate;
import java.util.Comparator;
import org.springframework.stereotype.Service;

/**
 * HU-017: la cita en sí no persiste el motivo de rechazo (vive en
 * `appointment_status_history.reason`, ver HU-023), así que para una
 * `REJECTED` se resuelve consultando la última transición registrada.
 */
@Service
public class ConsultarMisCitasService implements ConsultarMisCitasUseCase {

    private final CitaRepositoryPort citaRepository;
    private final HistorialEstadoCitaPort historialPort;

    public ConsultarMisCitasService(CitaRepositoryPort citaRepository, HistorialEstadoCitaPort historialPort) {
        this.citaRepository = citaRepository;
        this.historialPort = historialPort;
    }

    @Override
    public java.util.List<Resultado> listar(Long pacienteUsuarioId, EstadoCita estado, LocalDate fecha) {
        return citaRepository.listarPorPaciente(pacienteUsuarioId, estado, fecha).stream()
            .map(this::aResultado).toList();
    }

    private Resultado aResultado(Cita cita) {
        String motivoDecision = cita.getEstado() == EstadoCita.REJECTED ? motivoDeRechazo(cita.getId()) : null;
        return new Resultado(cita.getId(), cita.getSedeId(), cita.getProfesionalId(), cita.getEspecialidadId(),
            cita.getEstado().name(), cita.getInicio(), cita.getFin(), motivoDecision);
    }

    private String motivoDeRechazo(Long citaId) {
        return historialPort.listarPorCita(citaId).stream()
            .filter(t -> t.estado() == EstadoCita.REJECTED)
            .max(Comparator.comparing(t -> t.momento()))
            .map(t -> t.motivo())
            .orElse(null);
    }
}
