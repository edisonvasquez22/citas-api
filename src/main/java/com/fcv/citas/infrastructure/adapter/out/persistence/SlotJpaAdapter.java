package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.domain.model.SlotProfesional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `professional_slots` (HU-013/HU-014/HU-015). */
@Component
@Profile("!test")
public class SlotJpaAdapter implements SlotRepositoryPort {

    private final ProfessionalSlotJpaRepository slotRepository;

    public SlotJpaAdapter(ProfessionalSlotJpaRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public List<SlotProfesional> buscarLibres(Set<Long> profesionalIds, Long sedeId, LocalDate fecha) {
        return slotRepository.buscarLibres(profesionalIds, sedeId, fecha).stream().map(this::aDominio).toList();
    }

    @Override
    public Optional<List<Long>> buscarSlotsConsecutivosLibres(Long profesionalId, Long sedeId,
                                                                LocalDateTime inicio, int cantidad) {
        List<ProfessionalSlotJpaEntity> candidatos =
            slotRepository.buscarDesde(profesionalId, sedeId, inicio, PageRequest.of(0, cantidad));
        if (candidatos.size() < cantidad) {
            return Optional.empty();
        }
        List<ProfessionalSlotJpaEntity> ventana = candidatos.subList(0, cantidad);
        if (!ventana.get(0).getStartAt().equals(inicio)) {
            return Optional.empty();
        }
        for (ProfessionalSlotJpaEntity slot : ventana) {
            if (slot.getAppointmentId() != null) {
                return Optional.empty();
            }
        }
        for (int i = 0; i < ventana.size() - 1; i++) {
            if (!ventana.get(i).getEndAt().equals(ventana.get(i + 1).getStartAt())) {
                return Optional.empty();
            }
        }
        return Optional.of(ventana.stream().map(ProfessionalSlotJpaEntity::getId).toList());
    }

    @Override
    @Transactional
    public int reservarAtomicamente(List<Long> slotIds, Long citaId) {
        return slotRepository.reservarAtomicamente(slotIds, citaId);
    }

    @Override
    @Transactional
    public void liberarSlotsDeCita(Long citaId) {
        slotRepository.liberarSlotsDeCita(citaId);
    }

    private SlotProfesional aDominio(ProfessionalSlotJpaEntity entidad) {
        AvailabilityBlockJpaEntity bloque = entidad.getAvailabilityBlock();
        return new SlotProfesional(entidad.getId(), bloque.getId(), bloque.getProfessionalId(),
            bloque.getLocationId(), entidad.getStartAt(), entidad.getEndAt(), entidad.getAppointmentId());
    }
}
