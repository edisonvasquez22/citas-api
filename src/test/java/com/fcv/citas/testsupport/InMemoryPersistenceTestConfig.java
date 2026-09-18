package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Suministra los puertos de persistencia con dobles en memoria para pruebas
 * que corren con el perfil "test" (sin MySQL). Los adaptadores JPA reales
 * (`UsuarioJpaAdapter`, `RefreshTokenJpaAdapter`) están anotados
 * {@code @Profile("!test")}, así que no compiten por el mismo bean.
 */
@TestConfiguration
public class InMemoryPersistenceTestConfig {

    @Bean
    public UsuarioRepositoryPort usuarioRepositoryPort() {
        return new InMemoryUsuarioRepositoryAdapter();
    }

    @Bean
    public RefreshTokenStorePort refreshTokenStorePort() {
        return new InMemoryRefreshTokenStoreAdapter();
    }
}
