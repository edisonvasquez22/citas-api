package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.ConsultarMisCitasUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.application.port.out.SolicitudReprogramacionRepositoryPort;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.SolicitudReprogramacion;
import java.time.LocalDate;
import java.util.Comparator;
import org.springframework.stereotype.Service;

/**
 * HU-017: la cita en sí no persiste el motivo de rechazo (vive en
 * `appointment_status_history.reason`, ver HU-023), así que para una
 * `REJECTED` se resuelve consultando la última transición registrada.
 * También resuelve la última solicitud de reprogramación de cada cita
 * (HU-019/HU-020): es la única forma en que el paciente conoce el
 * desenlace de su propia solicitud, ya que el estado de la cita no
 * cambia mientras está PENDING (RN-10).
 */
@Service
public class ConsultarMisCitasService implements ConsultarMisCitasUseCase {

    private final CitaRepositoryPort citaRepository;
    private final HistorialEstadoCitaPort historialPort;
    private final SolicitudReprogramacionRepositoryPort solicitudReprogramacionRepository;

    public ConsultarMisCitasService(CitaRepositoryPort citaRepository, HistorialEstadoCitaPort historialPort,
                                     SolicitudReprogramacionRepositoryPort solicitudReprogramacionRepository) {
        this.citaRepository = citaRepository;
        this.historialPort = historialPort;
        this.solicitudReprogramacionRepository = solicitudReprogramacionRepository;
    }

    @Override
    public java.util.List<Resultado> listar(Long pacienteUsuarioId, EstadoCita estado, LocalDate fecha) {
        return citaRepository.listarPorPaciente(pacienteUsuarioId, estado, fecha).stream()
            .map(this::aResultado).toList();
    }

    private Resultado aResultado(Cita cita) {
        String motivoDecision = cita.getEstado() == EstadoCita.REJECTED ? motivoDeRechazo(cita.getId()) : null;
        ReprogramacionInfo reprogramacion = solicitudReprogramacionRepository.buscarUltimaPorCita(cita.getId())
            .map(this::aReprogramacionInfo).orElse(null);
        return new Resultado(cita.getId(), cita.getSedeId(), cita.getProfesionalId(), cita.getEspecialidadId(),
            cita.getEstado().name(), cita.getInicio(), cita.getFin(), motivoDecision, reprogramacion);
    }

    private ReprogramacionInfo aReprogramacionInfo(SolicitudReprogramacion solicitud) {
        return new ReprogramacionInfo(solicitud.getId(), solicitud.getEstado().name(),
            solicitud.getInicioSolicitado(), solicitud.getFinSolicitado(), solicitud.getMotivoDecision());
    }

    private String motivoDeRechazo(Long citaId) {
        return historialPort.listarPorCita(citaId).stream()
            .filter(t -> t.estado() == EstadoCita.REJECTED)
            .max(Comparator.comparing(t -> t.momento()))
            .map(t -> t.motivo())
            .orElse(null);
    }
}
