package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.testsupport.InMemoryEspecialidadRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-009: CA-01 a CA-03. */
class AdministrarEspecialidadesServiceTest {

    private AdministrarEspecialidadesService service;

    @BeforeEach
    void setUp() {
        service = new AdministrarEspecialidadesService(new InMemoryEspecialidadRepositoryAdapter());
    }

    @Test
    void crear_conDuracionValida_quedaEnElCatalogo() {
        var resultado = service.crear(new AdministrarEspecialidadesUseCase.CrearCommand("CARDIO", "Cardiología", 30,
            false, true));

        assertThat(resultado.id()).isNotNull();
        assertThat(service.listar()).extracting(AdministrarEspecialidadesUseCase.Resultado::codigo)
            .containsExactly("CARDIO");
    }

    @Test
    void crear_conDuracionInvalida_seRechaza() {
        assertThatThrownBy(() -> service.crear(
            new AdministrarEspecialidadesUseCase.CrearCommand("X", "X", 45, false, true)))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void cambiarEstado_desactivaLaEspecialidad() {
        var creada = service.crear(new AdministrarEspecialidadesUseCase.CrearCommand("CARDIO", "Cardiología", 30,
            false, true));

        var desactivada = service.cambiarEstado(creada.id(), false);

        assertThat(desactivada.activa()).isFalse();
    }
}
