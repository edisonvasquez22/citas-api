package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `appointments` (HU-014/HU-015/HU-016). */
@Component
@Profile("!test")
public class CitaJpaAdapter implements CitaRepositoryPort {

    private final CitaJpaRepository citaJpaRepository;
    private final AppointmentStatusJpaRepository statusJpaRepository;

    public CitaJpaAdapter(CitaJpaRepository citaJpaRepository, AppointmentStatusJpaRepository statusJpaRepository) {
        this.citaJpaRepository = citaJpaRepository;
        this.statusJpaRepository = statusJpaRepository;
    }

    @Override
    @Transactional
    public Cita guardar(Cita cita) {
        AppointmentStatusJpaEntity status = statusJpaRepository.findByCode(cita.getEstado().name())
            .orElseThrow(() -> new IllegalStateException(
                "Estado de cita no encontrado en el catálogo (¿corrió V2__seed_catalogos_fijos.sql?): "
                    + cita.getEstado()));
        CitaJpaEntity entidad = new CitaJpaEntity(cita.getId(), cita.getPacienteUsuarioId(),
            cita.getProfesionalId(), cita.getSedeId(), cita.getEspecialidadId(), status, cita.getMotivo(),
            cita.getInicio(), cita.getFin(), cita.getCreadoPorUsuarioId(), cita.getAprobadoPorUsuarioId(),
            cita.getAprobadoEn());
        return aDominio(citaJpaRepository.save(entidad));
    }

    @Override
    public Optional<Cita> buscarPorId(Long id) {
        return citaJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Cita> listarPorEstado(EstadoCita estado, Long sedeId, Long profesionalId, Long especialidadId,
                                       LocalDate fecha) {
        LocalDateTime desde = fecha == null ? null : fecha.atStartOfDay();
        LocalDateTime hasta = fecha == null ? null : fecha.plusDays(1).atStartOfDay();
        return citaJpaRepository.listarPorEstado(estado.name(), sedeId, profesionalId, especialidadId, desde, hasta)
            .stream().map(this::aDominio).toList();
    }

    private Cita aDominio(CitaJpaEntity entidad) {
        return Cita.reconstruir(entidad.getId(), entidad.getPatientUserId(), entidad.getProfessionalId(),
            entidad.getLocationId(), entidad.getSpecialtyId(), EstadoCita.valueOf(entidad.getStatus().getCode()),
            entidad.getReason(), entidad.getScheduledStartAt(), entidad.getScheduledEndAt(),
            entidad.getCreatedByUserId(), entidad.getApprovedByUserId(), entidad.getApprovedAt(), null);
    }
}
