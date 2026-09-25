package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.LocationRepositoryPort;
import com.fcv.citas.domain.model.Sede;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Adaptador JPA real de solo lectura sobre `locations`. Inactivo en el perfil "test". */
@Component
@Profile("!test")
public class LocationJpaAdapter implements LocationRepositoryPort {

    private final LocationJpaRepository locationJpaRepository;

    public LocationJpaAdapter(LocationJpaRepository locationJpaRepository) {
        this.locationJpaRepository = locationJpaRepository;
    }

    @Override
    public Optional<Sede> buscarPorId(Long id) {
        return locationJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Sede> listarActivas() {
        return locationJpaRepository.findByActiveTrue().stream().map(this::aDominio).toList();
    }

    private Sede aDominio(LocationJpaEntity entidad) {
        return new Sede(entidad.getId(), entidad.getCode(), entidad.getName(), entidad.isActive());
    }
}
