package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.SolicitarReprogramacionUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.application.port.out.SolicitudReprogramacionRepositoryPort;
import com.fcv.citas.domain.exception.HorarioNoDisponibleException;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.BloqueDisponibilidad;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.SolicitudReprogramacion;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-019: igual que HU-014/HU-015 en cuanto a retención atómica anti doble-reserva (RN-01), pero la
 * franja nueva se reserva bajo el MISMO {@code citaId} sin tocar la cita original (RN-10) — ver
 * {@code GestionarReprogramacionesService} para cómo se distinguen franja antigua/nueva al decidir.
 */
@Service
public class SolicitarReprogramacionService implements SolicitarReprogramacionUseCase {

    private final CitaRepositoryPort citaRepository;
    private final ProfesionalRepositoryPort profesionalRepository;
    private final SlotRepositoryPort slotRepository;
    private final SolicitudReprogramacionRepositoryPort solicitudRepository;

    public SolicitarReprogramacionService(CitaRepositoryPort citaRepository,
                                           ProfesionalRepositoryPort profesionalRepository,
                                           SlotRepositoryPort slotRepository,
                                           SolicitudReprogramacionRepositoryPort solicitudRepository) {
        this.citaRepository = citaRepository;
        this.profesionalRepository = profesionalRepository;
        this.slotRepository = slotRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    @Transactional
    public Resultado solicitar(Command command) {
        Cita cita = obtenerPropia(command.pacienteUsuarioId(), command.citaId());
        if (cita.getEstado() != EstadoCita.APPROVED) {
            throw new ValidacionNegocioException(
                "Solo una cita APPROVED puede reprogramarse (estado actual: " + cita.getEstado() + ")");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (!cita.getInicio().isAfter(ahora)) {
            throw new ValidacionNegocioException("Solo se puede reprogramar una cita futura");
        }

        LocalDateTime nuevoInicio = LocalDateTime.of(command.nuevaFecha(), command.nuevaHoraInicio());
        if (!nuevoInicio.isAfter(ahora)) {
            throw new ValidacionNegocioException("El nuevo horario debe ser futuro");
        }

        Profesional profesional = profesionalRepository.buscarPorId(cita.getProfesionalId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));
        if (!profesional.tieneSedeHabilitada(command.nuevaSedeId())) {
            throw new ValidacionNegocioException("El profesional no atiende en esa sede");
        }

        long duracionMinutos = Duration.between(cita.getInicio(), cita.getFin()).toMinutes();
        LocalDateTime nuevoFin = nuevoInicio.plusMinutes(duracionMinutos);
        int slotsNecesarios = (int) (duracionMinutos / BloqueDisponibilidad.DURACION_SLOT_MINUTOS);

        List<Long> nuevosSlotIds = slotRepository
            .buscarSlotsConsecutivosLibres(cita.getProfesionalId(), command.nuevaSedeId(), nuevoInicio,
                slotsNecesarios)
            .orElseThrow(() -> new HorarioNoDisponibleException("El horario solicitado ya no está disponible"));

        int reservados = slotRepository.reservarAtomicamente(nuevosSlotIds, cita.getId());
        if (reservados != nuevosSlotIds.size()) {
            slotRepository.liberarSlots(nuevosSlotIds);
            throw new HorarioNoDisponibleException(
                "El horario solicitado dejó de estar disponible mientras se confirmaba la solicitud");
        }

        SolicitudReprogramacion solicitud = SolicitudReprogramacion.solicitar(cita.getId(),
            command.pacienteUsuarioId(), command.nuevaSedeId(), cita.getInicio(), cita.getFin(), nuevoInicio,
            nuevoFin);
        SolicitudReprogramacion guardada = solicitudRepository.guardar(solicitud);

        return new Resultado(guardada.getId(), cita.getId(), guardada.getEstado().name(), nuevoInicio, nuevoFin);
    }

    private Cita obtenerPropia(Long pacienteUsuarioId, Long citaId) {
        Cita cita = citaRepository.buscarPorId(citaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));
        if (!cita.getPacienteUsuarioId().equals(pacienteUsuarioId)) {
            throw new RecursoNoEncontradoException("Cita no encontrada: " + citaId);
        }
        return cita;
    }
}
