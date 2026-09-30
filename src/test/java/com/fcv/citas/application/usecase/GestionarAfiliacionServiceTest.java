package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.AdministrarEpsUseCase;
import com.fcv.citas.application.port.in.AdministrarPlanesEpsUseCase;
import com.fcv.citas.application.port.in.GestionarAfiliacionUseCase;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.testsupport.InMemoryAfiliacionRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryEpsRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryPlanEpsRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-005: CA-01 a CA-03. */
class GestionarAfiliacionServiceTest {

    private GestionarAfiliacionService service;
    private AdministrarEpsService epsService;
    private AdministrarPlanesEpsService planService;
    private Long epsAId;
    private Long epsBId;
    private Long planDeAId;

    @BeforeEach
    void setUp() {
        var epsRepository = new InMemoryEpsRepositoryAdapter();
        var planRepository = new InMemoryPlanEpsRepositoryAdapter();
        epsService = new AdministrarEpsService(epsRepository);
        planService = new AdministrarPlanesEpsService(planRepository, epsRepository);
        service = new GestionarAfiliacionService(new InMemoryAfiliacionRepositoryAdapter(), epsRepository,
            planRepository);

        epsAId = epsService.crear(new AdministrarEpsUseCase.CrearCommand("EPS_A", "EPS A")).id();
        epsBId = epsService.crear(new AdministrarEpsUseCase.CrearCommand("EPS_B", "EPS B")).id();
        planDeAId = planService.crear(epsAId, new AdministrarPlanesEpsUseCase.CrearCommand(1L, "A-PLAN", "Plan A"))
            .id();
    }

    @Test
    void asociar_conDatosValidos_quedaComoAfiliacionVigente() {
        var resultado = service.asociar("1",
            new GestionarAfiliacionUseCase.AsociarCommand(epsAId, planDeAId, "AFIL-001"));

        assertThat(resultado.epsId()).isEqualTo(epsAId);
        assertThat(resultado.planId()).isEqualTo(planDeAId);
        assertThat(service.consultarVigente("1")).isPresent();
        assertThat(service.consultarVigente("1").get().numeroAfiliacion()).isEqualTo("AFIL-001");
    }

    @Test
    void asociar_planQueNoPerteneceALaEps_seRechaza() {
        assertThatThrownBy(() -> service.asociar("1",
            new GestionarAfiliacionUseCase.AsociarCommand(epsBId, planDeAId, "AFIL-001")))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void asociar_epsInactiva_seRechaza() {
        epsService.cambiarEstado(epsAId, false);

        assertThatThrownBy(() -> service.asociar("1",
            new GestionarAfiliacionUseCase.AsociarCommand(epsAId, planDeAId, "AFIL-001")))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void asociar_planInactivo_seRechaza() {
        planService.cambiarEstado(planDeAId, false);

        assertThatThrownBy(() -> service.asociar("1",
            new GestionarAfiliacionUseCase.AsociarCommand(epsAId, planDeAId, "AFIL-001")))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void asociar_reemplazaLaAfiliacionVigentePrevia() {
        service.asociar("1", new GestionarAfiliacionUseCase.AsociarCommand(epsAId, planDeAId, "AFIL-001"));
        Long planDeAId2 = planService.crear(epsAId,
            new AdministrarPlanesEpsUseCase.CrearCommand(2L, "A-PLAN-2", "Plan A2")).id();

        service.asociar("1", new GestionarAfiliacionUseCase.AsociarCommand(epsAId, planDeAId2, "AFIL-002"));

        var vigente = service.consultarVigente("1").orElseThrow();
        assertThat(vigente.numeroAfiliacion()).isEqualTo("AFIL-002");
        assertThat(vigente.planId()).isEqualTo(planDeAId2);
    }
}
