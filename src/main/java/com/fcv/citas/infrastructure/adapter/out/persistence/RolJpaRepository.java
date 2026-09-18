package com.fcv.citas.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Usada únicamente por {@link UsuarioJpaAdapter} (guardada tras @Profile("!test")). */
public interface RolJpaRepository extends JpaRepository<RolJpaEntity, Long> {

    Optional<RolJpaEntity> findByCode(String code);
}
