package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.EspecialidadRepositoryPort;
import com.fcv.citas.domain.model.Especialidad;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `specialties` (HU-009). Inactivo en el perfil "test". */
@Component
@Profile("!test")
public class EspecialidadJpaAdapter implements EspecialidadRepositoryPort {

    private final EspecialidadJpaRepository especialidadJpaRepository;

    public EspecialidadJpaAdapter(EspecialidadJpaRepository especialidadJpaRepository) {
        this.especialidadJpaRepository = especialidadJpaRepository;
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return codigo != null && especialidadJpaRepository.existsByCode(codigo);
    }

    @Override
    @Transactional
    public Especialidad guardar(Especialidad especialidad) {
        EspecialidadJpaEntity entidad = new EspecialidadJpaEntity(especialidad.getId(), especialidad.getCodigo(),
            especialidad.getNombre(), especialidad.getDuracionMinutos(), especialidad.isGeneral(),
            especialidad.isRequiereAprobacionAdmin(), especialidad.isActiva());
        return aDominio(especialidadJpaRepository.save(entidad));
    }

    @Override
    public Optional<Especialidad> buscarPorId(Long id) {
        return especialidadJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Especialidad> listar() {
        return especialidadJpaRepository.findAll().stream().map(this::aDominio).toList();
    }

    @Override
    public List<Especialidad> listarActivas() {
        return especialidadJpaRepository.findByActiveTrue().stream().map(this::aDominio).toList();
    }

    private Especialidad aDominio(EspecialidadJpaEntity entidad) {
        return Especialidad.reconstruir(entidad.getId(), entidad.getCode(), entidad.getName(),
            entidad.getAppointmentDurationMinutes(), entidad.isGeneral(), entidad.isRequiresAdminApproval(),
            entidad.isActive());
    }
}
