package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.GestionarSolicitudesEspecializadasUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.FuenteCambioEstado;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionarSolicitudesEspecializadasService implements GestionarSolicitudesEspecializadasUseCase {

    private final CitaRepositoryPort citaRepository;
    private final SlotRepositoryPort slotRepository;
    private final HistorialEstadoCitaPort historialPort;

    public GestionarSolicitudesEspecializadasService(CitaRepositoryPort citaRepository,
                                                       SlotRepositoryPort slotRepository,
                                                       HistorialEstadoCitaPort historialPort) {
        this.citaRepository = citaRepository;
        this.slotRepository = slotRepository;
        this.historialPort = historialPort;
    }

    @Override
    public List<Resumen> listarSolicitudes(Long sedeId, Long profesionalId, Long especialidadId, LocalDate fecha) {
        return citaRepository.listarPorEstado(EstadoCita.REQUESTED, sedeId, profesionalId, especialidadId, fecha)
            .stream().map(this::aResumen).toList();
    }

    @Override
    @Transactional
    public Resumen aprobar(Long adminUsuarioId, Long citaId) {
        Cita cita = obtener(citaId);
        Cita aprobada = cita.aprobar(adminUsuarioId, LocalDateTime.now());
        Cita guardada = citaRepository.guardar(aprobada);

        historialPort.registrar(TransicionEstadoCita.nueva(guardada.getId(), EstadoCita.APPROVED, adminUsuarioId,
            FuenteCambioEstado.ADMIN, null, LocalDateTime.now()));

        return aResumen(guardada);
    }

    @Override
    @Transactional
    public Resumen rechazar(Long adminUsuarioId, Long citaId, String motivo) {
        Cita cita = obtener(citaId);
        Cita rechazada = cita.rechazar(adminUsuarioId, motivo, LocalDateTime.now());
        Cita guardada = citaRepository.guardar(rechazada);

        // RN-09: el rechazo libera los slots retenidos por la solicitud.
        slotRepository.liberarSlotsDeCita(guardada.getId());

        historialPort.registrar(TransicionEstadoCita.nueva(guardada.getId(), EstadoCita.REJECTED, adminUsuarioId,
            FuenteCambioEstado.ADMIN, motivo, LocalDateTime.now()));

        // El motivo del rechazo se audita en el historial (appointment_status_history.reason,
        // ver HU-023); `appointments` no tiene columna propia para él (ver database/reference/db.sql),
        // así que la respuesta inmediata usa el `motivo` recibido, no el roundtrip de persistencia.
        return new Resumen(guardada.getId(), guardada.getPacienteUsuarioId(), guardada.getProfesionalId(),
            guardada.getSedeId(), guardada.getEspecialidadId(), guardada.getEstado().name(), guardada.getInicio(),
            guardada.getFin(), motivo);
    }

    private Cita obtener(Long citaId) {
        return citaRepository.buscarPorId(citaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));
    }

    private Resumen aResumen(Cita cita) {
        return new Resumen(cita.getId(), cita.getPacienteUsuarioId(), cita.getProfesionalId(), cita.getSedeId(),
            cita.getEspecialidadId(), cita.getEstado().name(), cita.getInicio(), cita.getFin(),
            cita.getMotivoDecision());
    }
}
