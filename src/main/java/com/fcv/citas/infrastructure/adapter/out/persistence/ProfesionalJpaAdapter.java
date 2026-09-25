package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Profesional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `professionals`/`professional_specialties`/`professional_locations`. */
@Component
@Profile("!test")
public class ProfesionalJpaAdapter implements ProfesionalRepositoryPort {

    private final ProfesionalJpaRepository profesionalJpaRepository;

    public ProfesionalJpaAdapter(ProfesionalJpaRepository profesionalJpaRepository) {
        this.profesionalJpaRepository = profesionalJpaRepository;
    }

    @Override
    public boolean existeCodigoProfesional(String codigoProfesional) {
        return codigoProfesional != null && profesionalJpaRepository.existsByProfessionalCode(codigoProfesional);
    }

    @Override
    public boolean existeMatricula(String matricula) {
        return matricula != null && profesionalJpaRepository.existsByLicenseNumber(matricula);
    }

    @Override
    @Transactional
    public Profesional guardar(Profesional profesional) {
        Set<EspecialidadAsignadaEmbeddable> especialidades = profesional.getEspecialidades().stream()
            .map(a -> new EspecialidadAsignadaEmbeddable(a.especialidadId(), a.primaria(), true))
            .collect(Collectors.toCollection(HashSet::new));
        Set<SedeAsignadaEmbeddable> sedes = profesional.getSedeIds().stream()
            .map(sedeId -> new SedeAsignadaEmbeddable(sedeId, true))
            .collect(Collectors.toCollection(HashSet::new));

        ProfesionalJpaEntity entidad = new ProfesionalJpaEntity(profesional.getId(), profesional.getUsuarioId(),
            profesional.getCodigoProfesional(), profesional.getMatricula(), profesional.isActivo(), especialidades,
            sedes);
        return aDominio(profesionalJpaRepository.save(entidad));
    }

    @Override
    public Optional<Profesional> buscarPorId(Long id) {
        return profesionalJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public Optional<Profesional> buscarPorUsuarioId(Long usuarioId) {
        return profesionalJpaRepository.findByUserId(usuarioId).map(this::aDominio);
    }

    @Override
    public List<Profesional> listarActivos() {
        return profesionalJpaRepository.findByActiveTrue().stream().map(this::aDominio).toList();
    }

    private Profesional aDominio(ProfesionalJpaEntity entidad) {
        Set<AsignacionEspecialidad> especialidades = entidad.getEspecialidades().stream()
            .map(e -> new AsignacionEspecialidad(e.getSpecialtyId(), e.isPrimary()))
            .collect(Collectors.toCollection(HashSet::new));
        Set<Long> sedeIds = entidad.getSedes().stream()
            .map(SedeAsignadaEmbeddable::getLocationId)
            .collect(Collectors.toCollection(HashSet::new));
        return Profesional.reconstruir(entidad.getId(), entidad.getUserId(), entidad.getProfessionalCode(),
            entidad.getLicenseNumber(), entidad.isActive(), especialidades, sedeIds);
    }
}
