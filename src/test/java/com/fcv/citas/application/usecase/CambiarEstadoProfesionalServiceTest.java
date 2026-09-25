package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-011: CA-01/CA-02 (desactivación efectiva, reactivación). */
class CambiarEstadoProfesionalServiceTest {

    private InMemoryProfesionalRepositoryAdapter repository;
    private CambiarEstadoProfesionalService service;
    private Long profesionalId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProfesionalRepositoryAdapter();
        service = new CambiarEstadoProfesionalService(repository);
        Profesional guardado = repository.guardar(Profesional.registrar(1L, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(10L, true)), Set.of(1L)));
        profesionalId = guardado.getId();
    }

    @Test
    void cambiarEstado_desactiva() {
        var resultado = service.cambiarEstado(profesionalId, false);

        assertThat(resultado.activo()).isFalse();
        assertThat(repository.buscarPorId(profesionalId)).get().extracting(Profesional::isActivo).isEqualTo(false);
    }

    @Test
    void cambiarEstado_reactiva() {
        service.cambiarEstado(profesionalId, false);

        var resultado = service.cambiarEstado(profesionalId, true);

        assertThat(resultado.activo()).isTrue();
    }
}
