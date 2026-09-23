package com.fcv.citas.infrastructure.adapter.out.persistence;

import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.domain.model.Usuario;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador JPA real sobre `users`/`user_roles` (ver
 * citas-api/docs/db-design/MODELO_3FN.md). Reemplaza al adaptador temporal en
 * memoria; solo está activo fuera del perfil "test" (los tests usan
 * `testsupport.InMemoryUsuarioRepositoryAdapter`, ver AuthFlowIntegrationTest).
 */
@Component
@Profile("!test")
public class UsuarioJpaAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final RolJpaRepository rolJpaRepository;

    public UsuarioJpaAdapter(UsuarioJpaRepository usuarioJpaRepository, RolJpaRepository rolJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.rolJpaRepository = rolJpaRepository;
    }

    @Override
    public boolean existePorEmail(String email) {
        return email != null && usuarioJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existePorNumeroDocumento(String numeroDocumento) {
        return numeroDocumento != null && usuarioJpaRepository.existsByDocumentNumber(numeroDocumento);
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        Set<RolJpaEntity> rolesEntidad = usuario.getRoles().stream()
            .map(rol -> rolJpaRepository.findByCode(rol.name())
                .orElseThrow(() -> new IllegalStateException(
                    "Rol no encontrado en el catálogo (¿corrió V2__seed_catalogos_fijos.sql?): " + rol.name())))
            .collect(Collectors.toSet());

        UsuarioJpaEntity entidad = new UsuarioJpaEntity(
            usuario.getId() == null ? null : Long.valueOf(usuario.getId()),
            usuario.getNombres(), usuario.getApellidos(), usuario.getTipoDocumento(),
            usuario.getNumeroDocumento(), usuario.getEmail(), usuario.getTelefono(), usuario.getPasswordHash(),
            usuario.isActivo(), rolesEntidad
        );
        UsuarioJpaEntity guardada = usuarioJpaRepository.save(entidad);
        return Usuario.reconstruir(
            String.valueOf(guardada.getId()), usuario.getNombres(), usuario.getApellidos(),
            usuario.getTipoDocumento(), usuario.getNumeroDocumento(), usuario.getEmail(), usuario.getTelefono(),
            usuario.getPasswordHash(), usuario.getRoles(), usuario.isActivo()
        );
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return usuarioJpaRepository.findByEmail(email).map(this::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        Long idNumerico;
        try {
            idNumerico = Long.valueOf(id);
        } catch (NumberFormatException | NullPointerException e) {
            return Optional.empty();
        }
        return usuarioJpaRepository.findById(idNumerico).map(this::aDominio);
    }

    private Usuario aDominio(UsuarioJpaEntity entidad) {
        Set<RolNombre> roles = entidad.getRoles().stream()
            .map(rolEntidad -> RolNombre.valueOf(rolEntidad.getCode()))
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(RolNombre.class)));
        return Usuario.reconstruir(
            String.valueOf(entidad.getId()), entidad.getFirstName(), entidad.getLastName(), entidad.getDocumentType(),
            entidad.getDocumentNumber(), entidad.getEmail(), entidad.getPhone(), entidad.getPasswordHash(),
            roles, entidad.isActive()
        );
    }
}
