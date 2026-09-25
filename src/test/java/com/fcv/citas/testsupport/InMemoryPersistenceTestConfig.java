package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.BloqueDisponibilidadRepositoryPort;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.EspecialidadRepositoryPort;
import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.application.port.out.LocationRepositoryPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Suministra los puertos de persistencia con dobles en memoria para pruebas
 * que corren con el perfil "test" (sin MySQL). Los adaptadores JPA reales
 * (`UsuarioJpaAdapter`, `RefreshTokenJpaAdapter`, etc.) están anotados
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

    @Bean
    public EspecialidadRepositoryPort especialidadRepositoryPort() {
        return new InMemoryEspecialidadRepositoryAdapter();
    }

    @Bean
    public LocationRepositoryPort locationRepositoryPort() {
        return new InMemoryLocationRepositoryAdapter();
    }

    @Bean
    public ProfesionalRepositoryPort profesionalRepositoryPort() {
        return new InMemoryProfesionalRepositoryAdapter();
    }

    @Bean
    public InMemoryDisponibilidadStore disponibilidadStore() {
        return new InMemoryDisponibilidadStore();
    }

    @Bean
    public BloqueDisponibilidadRepositoryPort bloqueDisponibilidadRepositoryPort(InMemoryDisponibilidadStore store) {
        return new InMemoryBloqueDisponibilidadRepositoryAdapter(store);
    }

    @Bean
    public SlotRepositoryPort slotRepositoryPort(InMemoryDisponibilidadStore store) {
        return new InMemorySlotRepositoryAdapter(store);
    }

    @Bean
    public CitaRepositoryPort citaRepositoryPort() {
        return new InMemoryCitaRepositoryAdapter();
    }

    @Bean
    public HistorialEstadoCitaPort historialEstadoCitaPort() {
        return new InMemoryHistorialEstadoCitaAdapter();
    }
}
