package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.PlanEpsRepositoryPort;
import com.fcv.citas.domain.model.PlanEps;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `eps_plans` (HU-008). Inactivo en el perfil "test". */
@Component
@Profile("!test")
public class PlanEpsJpaAdapter implements PlanEpsRepositoryPort {

    private final PlanEpsJpaRepository planEpsJpaRepository;

    public PlanEpsJpaAdapter(PlanEpsJpaRepository planEpsJpaRepository) {
        this.planEpsJpaRepository = planEpsJpaRepository;
    }

    @Override
    public boolean existePorEpsYCodigo(Long epsId, String codigo) {
        return epsId != null && codigo != null && planEpsJpaRepository.existsByEpsIdAndCode(epsId, codigo);
    }

    @Override
    @Transactional
    public PlanEps guardar(PlanEps plan) {
        PlanEpsJpaEntity entidad = new PlanEpsJpaEntity(plan.getId(), plan.getEpsId(), plan.getRegimenId(),
            plan.getCodigo(), plan.getNombre(), plan.isActivo());
        return aDominio(planEpsJpaRepository.save(entidad));
    }

    @Override
    public Optional<PlanEps> buscarPorId(Long id) {
        return planEpsJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<PlanEps> listarPorEps(Long epsId) {
        return planEpsJpaRepository.findByEpsId(epsId).stream().map(this::aDominio).toList();
    }

    @Override
    public List<PlanEps> listarActivosPorEps(Long epsId) {
        return planEpsJpaRepository.findByEpsIdAndActiveTrue(epsId).stream().map(this::aDominio).toList();
    }

    private PlanEps aDominio(PlanEpsJpaEntity entidad) {
        return PlanEps.reconstruir(entidad.getId(), entidad.getEpsId(), entidad.getRegimeId(), entidad.getCode(),
            entidad.getName(), entidad.isActive());
    }
}
