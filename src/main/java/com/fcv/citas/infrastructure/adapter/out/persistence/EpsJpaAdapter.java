package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.EpsRepositoryPort;
import com.fcv.citas.domain.model.Eps;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA real sobre `eps` (HU-007). Inactivo en el perfil "test". */
@Component
@Profile("!test")
public class EpsJpaAdapter implements EpsRepositoryPort {

    private final EpsJpaRepository epsJpaRepository;

    public EpsJpaAdapter(EpsJpaRepository epsJpaRepository) {
        this.epsJpaRepository = epsJpaRepository;
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return codigo != null && epsJpaRepository.existsByCode(codigo);
    }

    @Override
    @Transactional
    public Eps guardar(Eps eps) {
        EpsJpaEntity entidad = new EpsJpaEntity(eps.getId(), eps.getCodigo(), eps.getNombre(), eps.isActiva());
        return aDominio(epsJpaRepository.save(entidad));
    }

    @Override
    public Optional<Eps> buscarPorId(Long id) {
        return epsJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Eps> listar() {
        return epsJpaRepository.findAll().stream().map(this::aDominio).toList();
    }

    @Override
    public List<Eps> listarActivas() {
        return epsJpaRepository.findByActiveTrue().stream().map(this::aDominio).toList();
    }

    private Eps aDominio(EpsJpaEntity entidad) {
        return Eps.reconstruir(entidad.getId(), entidad.getCode(), entidad.getName(), entidad.isActive());
    }
}
