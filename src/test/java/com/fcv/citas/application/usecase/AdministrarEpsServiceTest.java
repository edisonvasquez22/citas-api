package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.AdministrarEpsUseCase;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.testsupport.InMemoryEpsRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-007: CA-01 y CA-02. */
class AdministrarEpsServiceTest {

    private AdministrarEpsService service;

    @BeforeEach
    void setUp() {
        service = new AdministrarEpsService(new InMemoryEpsRepositoryAdapter());
    }

    @Test
    void crear_conCodigoNuevo_quedaEnElCatalogo() {
        var resultado = service.crear(new AdministrarEpsUseCase.CrearCommand("EPS_X", "EPS de prueba"));

        assertThat(resultado.id()).isNotNull();
        assertThat(resultado.activa()).isTrue();
        assertThat(service.listar()).extracting(AdministrarEpsUseCase.Resultado::codigo).containsExactly("EPS_X");
    }

    @Test
    void crear_conCodigoDuplicado_seRechaza() {
        service.crear(new AdministrarEpsUseCase.CrearCommand("EPS_X", "EPS de prueba"));

        assertThatThrownBy(() -> service.crear(new AdministrarEpsUseCase.CrearCommand("EPS_X", "Otra")))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void cambiarEstado_desactivaLaEps() {
        var creada = service.crear(new AdministrarEpsUseCase.CrearCommand("EPS_X", "EPS de prueba"));

        var desactivada = service.cambiarEstado(creada.id(), false);

        assertThat(desactivada.activa()).isFalse();
    }
}
