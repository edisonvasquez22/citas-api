package com.fcv.citas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.domain.exception.ValidacionNegocioException;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** HU-010: CA-01/CA-02 (alta exitosa, especialidad primaria única). */
class ProfesionalTest {

    @Test
    void registrar_conUnaEspecialidadPrimariaYUnaSede_creaElProfesional() {
        Profesional profesional = Profesional.registrar(1L, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(10L, true)), Set.of(1L));

        assertThat(profesional.getId()).isNull();
        assertThat(profesional.isActivo()).isTrue();
        assertThat(profesional.tieneEspecialidadActiva(10L)).isTrue();
        assertThat(profesional.tieneSedeHabilitada(1L)).isTrue();
    }

    @Test
    void registrar_conDosEspecialidadesPrimarias_lanzaValidacionNegocio() {
        Set<AsignacionEspecialidad> especialidades = Set.of(
            new AsignacionEspecialidad(10L, true),
            new AsignacionEspecialidad(11L, true)
        );

        assertThatThrownBy(() -> Profesional.registrar(1L, "PROF-001", "MAT-001", especialidades, Set.of(1L)))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void registrar_sinEspecialidades_lanzaValidacionNegocio() {
        assertThatThrownBy(() -> Profesional.registrar(1L, "PROF-001", "MAT-001", Set.of(), Set.of(1L)))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void registrar_sinSedes_lanzaValidacionNegocio() {
        Set<AsignacionEspecialidad> especialidades = Set.of(new AsignacionEspecialidad(10L, true));

        assertThatThrownBy(() -> Profesional.registrar(1L, "PROF-001", "MAT-001", especialidades, Set.of()))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void cambiarEstado_desactivaSinTocarEspecialidadesNiSedes() {
        Profesional profesional = Profesional.registrar(1L, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(10L, true)), Set.of(1L));

        Profesional inactivo = profesional.cambiarEstado(false);

        assertThat(inactivo.isActivo()).isFalse();
        assertThat(inactivo.getEspecialidades()).isEqualTo(profesional.getEspecialidades());
        assertThat(inactivo.getSedeIds()).isEqualTo(profesional.getSedeIds());
    }
}
