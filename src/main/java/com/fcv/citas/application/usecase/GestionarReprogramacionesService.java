package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.GestionarReprogramacionesUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.application.port.out.SolicitudReprogramacionRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoSolicitudReprogramacion;
import com.fcv.citas.domain.model.SolicitudReprogramacion;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-020: durante el PENDING, tanto la franja antigua como la nueva de la cita están retenidas bajo el
 * mismo {@code citaId} (RN-10, ver {@code SolicitarReprogramacionService}). Aprobar libera la antigua y
 * deja la cita apuntando al nuevo horario/sede; rechazar libera solo la nueva y no toca la cita.
 */
@Service
public class GestionarReprogramacionesService implements GestionarReprogramacionesUseCase {

    private final SolicitudReprogramacionRepositoryPort solicitudRepository;
    private final CitaRepositoryPort citaRepository;
    private final SlotRepositoryPort slotRepository;

    public GestionarReprogramacionesService(SolicitudReprogramacionRepositoryPort solicitudRepository,
                                             CitaRepositoryPort citaRepository, SlotRepositoryPort slotRepository) {
        this.solicitudRepository = solicitudRepository;
        this.citaRepository = citaRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    public List<Resumen> listarPendientes() {
        return solicitudRepository.listarPorEstado(EstadoSolicitudReprogramacion.PENDING).stream()
            .map(this::aResumen)
            .toList();
    }

    @Override
    @Transactional
    public Resumen aprobar(Long adminUsuarioId, Long solicitudId) {
        SolicitudReprogramacion solicitud = obtener(solicitudId);
        Cita cita = obtenerCita(solicitud.getCitaId());

        List<Long> slotsAntiguos = slotRepository.listarIdsDeCitaEnRango(cita.getId(), solicitud.getInicioAnterior(),
            solicitud.getFinAnterior());
        slotRepository.liberarSlots(slotsAntiguos);

        citaRepository.guardar(cita.reprogramar(solicitud.getSedeSolicitadaId(), solicitud.getInicioSolicitado(),
            solicitud.getFinSolicitado()));

        SolicitudReprogramacion aprobada = solicitudRepository.guardar(
            solicitud.aprobar(adminUsuarioId, LocalDateTime.now()));
        return aResumen(aprobada);
    }

    @Override
    @Transactional
    public Resumen rechazar(Long adminUsuarioId, Long solicitudId, String motivo) {
        SolicitudReprogramacion solicitud = obtener(solicitudId);
        Cita cita = obtenerCita(solicitud.getCitaId());

        List<Long> slotsNuevos = slotRepository.listarIdsDeCitaEnRango(cita.getId(), solicitud.getInicioSolicitado(),
            solicitud.getFinSolicitado());
        slotRepository.liberarSlots(slotsNuevos);

        SolicitudReprogramacion rechazada = solicitudRepository.guardar(
            solicitud.rechazar(adminUsuarioId, motivo, LocalDateTime.now()));
        return aResumen(rechazada);
    }

    private SolicitudReprogramacion obtener(Long solicitudId) {
        return solicitudRepository.buscarPorId(solicitudId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "Solicitud de reprogramación no encontrada: " + solicitudId));
    }

    private Cita obtenerCita(Long citaId) {
        return citaRepository.buscarPorId(citaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));
    }

    private Resumen aResumen(SolicitudReprogramacion solicitud) {
        Long profesionalId = citaRepository.buscarPorId(solicitud.getCitaId())
            .map(Cita::getProfesionalId)
            .orElse(null);
        return new Resumen(solicitud.getId(), solicitud.getCitaId(), profesionalId, solicitud.getSedeSolicitadaId(),
            solicitud.getEstado().name(), solicitud.getInicioAnterior(), solicitud.getFinAnterior(),
            solicitud.getInicioSolicitado(), solicitud.getFinSolicitado(), solicitud.getMotivoDecision());
    }
}
