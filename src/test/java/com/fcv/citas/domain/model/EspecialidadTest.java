package com.fcv.citas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.domain.exception.ValidacionNegocioException;
import org.junit.jupiter.api.Test;

/** HU-009: CA-01/CA-02 (duración válida/ inválida). */
class EspecialidadTest {

    @Test
    void crear_conDuracionValida_creaLaEspecialidad() {
        Especialidad especialidad = Especialidad.crear("CARDIO", "Cardiología", 30, false, true);

        assertThat(especialidad.getId()).isNull();
        assertThat(especialidad.getDuracionMinutos()).isEqualTo(30);
        assertThat(especialidad.isActiva()).isTrue();
    }

    @Test
    void crear_conDuracion60_creaLaEspecialidad() {
        Especialidad especialidad = Especialidad.crear("NEURO", "Neurología", 60, false, true);

        assertThat(especialidad.getDuracionMinutos()).isEqualTo(60);
    }

    @Test
    void crear_conDuracionInvalida_lanzaValidacionNegocio() {
        assertThatThrownBy(() -> Especialidad.crear("X", "X", 45, false, true))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void cambiarEstado_desactiva() {
        Especialidad especialidad = Especialidad.reconstruir(1L, "CARDIO", "Cardiología", 30, false, true, true);

        Especialidad desactivada = especialidad.cambiarEstado(false);

        assertThat(desactivada.isActiva()).isFalse();
        assertThat(desactivada.getId()).isEqualTo(1L);
    }
}
