package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.AdministrarEpsUseCase;
import com.fcv.citas.application.port.in.AdministrarPlanesEpsUseCase;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.testsupport.InMemoryEpsRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryPlanEpsRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-008: CA-01 y CA-02. */
class AdministrarPlanesEpsServiceTest {

    private AdministrarPlanesEpsService service;
    private Long epsId;

    @BeforeEach
    void setUp() {
        var epsRepository = new InMemoryEpsRepositoryAdapter();
        service = new AdministrarPlanesEpsService(new InMemoryPlanEpsRepositoryAdapter(), epsRepository);
        var eps = new AdministrarEpsService(epsRepository).crear(
            new AdministrarEpsUseCase.CrearCommand("EPS_X", "EPS de prueba"));
        epsId = eps.id();
    }

    @Test
    void crear_paraEpsExistente_quedaEnElCatalogo() {
        var resultado = service.crear(epsId, new AdministrarPlanesEpsUseCase.CrearCommand(1L, "PLAN_X", "Plan X"));

        assertThat(resultado.id()).isNotNull();
        assertThat(resultado.epsId()).isEqualTo(epsId);
        assertThat(service.listarPorEps(epsId)).extracting(AdministrarPlanesEpsUseCase.Resultado::codigo)
            .containsExactly("PLAN_X");
    }

    @Test
    void crear_paraEpsInexistente_seRechaza() {
        assertThatThrownBy(() -> service.crear(999L,
            new AdministrarPlanesEpsUseCase.CrearCommand(1L, "PLAN_X", "Plan X")))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crear_conCodigoDuplicadoEnLaMismaEps_seRechaza() {
        service.crear(epsId, new AdministrarPlanesEpsUseCase.CrearCommand(1L, "PLAN_X", "Plan X"));

        assertThatThrownBy(() -> service.crear(epsId,
            new AdministrarPlanesEpsUseCase.CrearCommand(1L, "PLAN_X", "Otro")))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void cambiarEstado_desactivaElPlan() {
        var creado = service.crear(epsId, new AdministrarPlanesEpsUseCase.CrearCommand(1L, "PLAN_X", "Plan X"));

        var desactivado = service.cambiarEstado(creado.id(), false);

        assertThat(desactivado.activo()).isFalse();
    }
}
