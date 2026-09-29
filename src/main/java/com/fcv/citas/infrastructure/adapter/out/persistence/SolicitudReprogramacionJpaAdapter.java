package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.SolicitudReprogramacionRepositoryPort;
import com.fcv.citas.domain.model.EstadoSolicitudReprogramacion;
import com.fcv.citas.domain.model.SolicitudReprogramacion;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `reschedule_requests` (HU-019/HU-020). */
@Component
@Profile("!test")
public class SolicitudReprogramacionJpaAdapter implements SolicitudReprogramacionRepositoryPort {

    private final RescheduleRequestJpaRepository repository;
    private final RescheduleRequestStatusJpaRepository statusRepository;

    public SolicitudReprogramacionJpaAdapter(RescheduleRequestJpaRepository repository,
                                              RescheduleRequestStatusJpaRepository statusRepository) {
        this.repository = repository;
        this.statusRepository = statusRepository;
    }

    @Override
    @Transactional
    public SolicitudReprogramacion guardar(SolicitudReprogramacion solicitud) {
        RescheduleRequestStatusJpaEntity status = statusRepository.findByCode(solicitud.getEstado().name())
            .orElseThrow(() -> new IllegalStateException(
                "Estado de reprogramación no encontrado en el catálogo (¿corrió V2__seed_catalogos_fijos.sql?): "
                    + solicitud.getEstado()));
        RescheduleRequestJpaEntity entidad = new RescheduleRequestJpaEntity(solicitud.getId(), solicitud.getCitaId(),
            solicitud.getSolicitadoPorUsuarioId(), solicitud.getSedeSolicitadaId(), status,
            solicitud.getInicioAnterior(), solicitud.getFinAnterior(), solicitud.getInicioSolicitado(),
            solicitud.getFinSolicitado(), solicitud.getMotivoDecision(), solicitud.getDecididoPorUsuarioId(),
            solicitud.getDecididoEn());
        return aDominio(repository.save(entidad));
    }

    @Override
    public Optional<SolicitudReprogramacion> buscarPorId(Long id) {
        return repository.findById(id).map(this::aDominio);
    }

    @Override
    public List<SolicitudReprogramacion> listarPorEstado(EstadoSolicitudReprogramacion estado) {
        return repository.listarPorEstado(estado.name()).stream().map(this::aDominio).toList();
    }

    @Override
    public Optional<SolicitudReprogramacion> buscarUltimaPorCita(Long citaId) {
        return repository.findFirstByAppointmentIdOrderByIdDesc(citaId).map(this::aDominio);
    }

    private SolicitudReprogramacion aDominio(RescheduleRequestJpaEntity entidad) {
        return SolicitudReprogramacion.reconstruir(entidad.getId(), entidad.getAppointmentId(),
            entidad.getRequestedByUserId(), entidad.getRequestedLocationId(),
            EstadoSolicitudReprogramacion.valueOf(entidad.getStatus().getCode()), entidad.getPreviousStartAt(),
            entidad.getPreviousEndAt(), entidad.getRequestedStartAt(), entidad.getRequestedEndAt(),
            entidad.getDecisionReason(), entidad.getDecidedByUserId(), entidad.getDecidedAt());
    }
}
