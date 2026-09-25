package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.SolicitarCitaGeneralUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.EspecialidadRepositoryPort;
import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.domain.exception.HorarioNoDisponibleException;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.BloqueDisponibilidad;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.Especialidad;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.FuenteCambioEstado;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-014: primer flujo de creación de citas real. La retención del horario es
 * atómica (RN-01) vía {@link SlotRepositoryPort#reservarAtomicamente}: bajo
 * concurrencia, solo una solicitud sobre el mismo horario puede ganar (ver
 * SolicitarCitaGeneralServiceTest, prueba de doble reserva pedida por
 * GUIA_SESIONES_S2_S6.md S3).
 */
@Service
public class SolicitarCitaGeneralService implements SolicitarCitaGeneralUseCase {

    private final EspecialidadRepositoryPort especialidadRepository;
    private final ProfesionalRepositoryPort profesionalRepository;
    private final SlotRepositoryPort slotRepository;
    private final CitaRepositoryPort citaRepository;
    private final HistorialEstadoCitaPort historialPort;

    public SolicitarCitaGeneralService(EspecialidadRepositoryPort especialidadRepository,
                                        ProfesionalRepositoryPort profesionalRepository,
                                        SlotRepositoryPort slotRepository, CitaRepositoryPort citaRepository,
                                        HistorialEstadoCitaPort historialPort) {
        this.especialidadRepository = especialidadRepository;
        this.profesionalRepository = profesionalRepository;
        this.slotRepository = slotRepository;
        this.citaRepository = citaRepository;
        this.historialPort = historialPort;
    }

    @Override
    @Transactional
    public Resultado solicitar(Command command) {
        Especialidad especialidad = especialidadRepository.buscarPorId(command.especialidadId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada: " + command.especialidadId()));
        if (!especialidad.isActiva() || !especialidad.isGeneral()) {
            throw new ValidacionNegocioException("Esta operación solo admite la especialidad general (Medicina General)");
        }

        Profesional profesional = profesionalRepository.buscarPorId(command.profesionalId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado: " + command.profesionalId()));
        if (!profesional.isActivo()) {
            throw new ValidacionNegocioException("El profesional no está activo");
        }
        if (!profesional.tieneSedeHabilitada(command.sedeId())) {
            throw new ValidacionNegocioException("El profesional no atiende en esa sede");
        }
        if (!profesional.tieneEspecialidadActiva(command.especialidadId())) {
            throw new ValidacionNegocioException("El profesional no atiende esa especialidad");
        }

        LocalDateTime inicio = LocalDateTime.of(command.fecha(), command.horaInicio());
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new ValidacionNegocioException("No se pueden reservar horarios en el pasado");
        }

        int slotsNecesarios = especialidad.getDuracionMinutos() / BloqueDisponibilidad.DURACION_SLOT_MINUTOS;
        List<Long> slotIds = slotRepository
            .buscarSlotsConsecutivosLibres(command.profesionalId(), command.sedeId(), inicio, slotsNecesarios)
            .orElseThrow(() -> new HorarioNoDisponibleException("El horario solicitado ya no está disponible"));
        LocalDateTime fin = inicio.plusMinutes((long) especialidad.getDuracionMinutos());

        Cita cita = Cita.solicitarGeneral(command.pacienteUsuarioId(), command.profesionalId(), command.sedeId(),
            command.especialidadId(), command.motivo(), inicio, fin);
        Cita guardada = citaRepository.guardar(cita);

        int reservados = slotRepository.reservarAtomicamente(slotIds, guardada.getId());
        if (reservados != slotIds.size()) {
            slotRepository.liberarSlotsDeCita(guardada.getId());
            throw new HorarioNoDisponibleException(
                "El horario solicitado dejó de estar disponible mientras se confirmaba la reserva");
        }

        historialPort.registrar(TransicionEstadoCita.nueva(guardada.getId(), EstadoCita.APPROVED,
            command.pacienteUsuarioId(), FuenteCambioEstado.SYSTEM, null, LocalDateTime.now()));

        return new Resultado(guardada.getId(), EstadoCita.APPROVED.name(), inicio, fin);
    }
}
