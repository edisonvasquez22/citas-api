package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.BloqueDisponibilidadRepositoryPort;
import com.fcv.citas.domain.model.BloqueDisponibilidad;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `availability_blocks` + `professional_slots` (HU-012). */
@Component
@Profile("!test")
public class BloqueDisponibilidadJpaAdapter implements BloqueDisponibilidadRepositoryPort {

    private final AvailabilityBlockJpaRepository blockRepository;
    private final ProfessionalSlotJpaRepository slotRepository;

    public BloqueDisponibilidadJpaAdapter(AvailabilityBlockJpaRepository blockRepository,
                                           ProfessionalSlotJpaRepository slotRepository) {
        this.blockRepository = blockRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    public boolean existeSolapamiento(Long profesionalId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                       Long excluirBloqueId) {
        return blockRepository.existeSolapamiento(profesionalId, fecha, horaInicio, horaFin, excluirBloqueId);
    }

    @Override
    @Transactional
    public BloqueDisponibilidad guardar(BloqueDisponibilidad bloque) {
        AvailabilityBlockJpaEntity guardado = blockRepository.save(aEntidad(bloque));
        crearSlots(guardado, bloque);
        return aDominio(guardado);
    }

    @Override
    public Optional<BloqueDisponibilidad> buscarPorId(Long id) {
        return blockRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<BloqueDisponibilidad> listarPorProfesional(Long profesionalId) {
        return blockRepository.findByProfessionalId(profesionalId).stream().map(this::aDominio).toList();
    }

    @Override
    public boolean tieneSlotsComprometidos(Long bloqueId) {
        return slotRepository.existsByAvailabilityBlockIdAndAppointmentIdIsNotNull(bloqueId);
    }

    @Override
    @Transactional
    public BloqueDisponibilidad actualizarHorario(BloqueDisponibilidad bloqueActualizado) {
        AvailabilityBlockJpaEntity guardado = blockRepository.save(aEntidad(bloqueActualizado));
        slotRepository.deleteByAvailabilityBlockId(guardado.getId());
        crearSlots(guardado, bloqueActualizado);
        return aDominio(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long bloqueId) {
        slotRepository.deleteByAvailabilityBlockId(bloqueId);
        blockRepository.deleteById(bloqueId);
    }

    private void crearSlots(AvailabilityBlockJpaEntity blockEntity, BloqueDisponibilidad bloque) {
        List<ProfessionalSlotJpaEntity> slots = bloque.generarSlots().stream()
            .map(rango -> new ProfessionalSlotJpaEntity(blockEntity, rango.inicio(), rango.fin()))
            .toList();
        slotRepository.saveAll(slots);
    }

    private AvailabilityBlockJpaEntity aEntidad(BloqueDisponibilidad bloque) {
        return new AvailabilityBlockJpaEntity(bloque.getId(), bloque.getProfesionalId(), bloque.getSedeId(),
            bloque.getFecha(), bloque.getHoraInicio(), bloque.getHoraFin(), bloque.isActivo());
    }

    private BloqueDisponibilidad aDominio(AvailabilityBlockJpaEntity entidad) {
        return BloqueDisponibilidad.reconstruir(entidad.getId(), entidad.getProfessionalId(),
            entidad.getLocationId(), entidad.getAvailableDate(), entidad.getStartTime(), entidad.getEndTime(),
            entidad.isActive());
    }
}
