package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.AfiliacionRepositoryPort;
import com.fcv.citas.domain.model.Afiliacion;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `user_insurance_affiliations` (HU-005). Inactivo en el perfil "test". */
@Component
@Profile("!test")
public class AfiliacionJpaAdapter implements AfiliacionRepositoryPort {

    private final AfiliacionJpaRepository afiliacionJpaRepository;

    public AfiliacionJpaAdapter(AfiliacionJpaRepository afiliacionJpaRepository) {
        this.afiliacionJpaRepository = afiliacionJpaRepository;
    }

    @Override
    public Optional<Afiliacion> buscarVigentePorUsuario(Long usuarioId) {
        return afiliacionJpaRepository.findByUserIdAndCurrentTrue(usuarioId).map(this::aDominio);
    }

    @Override
    public Optional<Afiliacion> buscarPorUsuarioPlanYNumero(Long usuarioId, Long planId, String numeroAfiliacion) {
        return afiliacionJpaRepository.findByUserIdAndPlanIdAndMembershipNumber(usuarioId, planId, numeroAfiliacion)
            .map(this::aDominio);
    }

    @Override
    @Transactional
    public Afiliacion guardar(Afiliacion afiliacion) {
        AfiliacionJpaEntity entidad = new AfiliacionJpaEntity(afiliacion.getId(), afiliacion.getUsuarioId(),
            afiliacion.getPlanId(), afiliacion.getNumeroAfiliacion(), afiliacion.isVigente(),
            afiliacion.getVigenteDesde());
        return aDominio(afiliacionJpaRepository.save(entidad));
    }

    private Afiliacion aDominio(AfiliacionJpaEntity entidad) {
        return Afiliacion.reconstruir(entidad.getId(), entidad.getUserId(), entidad.getPlanId(),
            entidad.getMembershipNumber(), entidad.isCurrent(), entidad.getValidFrom());
    }
}
