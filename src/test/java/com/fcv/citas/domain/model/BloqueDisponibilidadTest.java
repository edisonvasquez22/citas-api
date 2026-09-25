package com.fcv.citas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.domain.exception.ValidacionNegocioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/** HU-012: CA-01 a CA-03 (creación válida, bloque en el pasado, discretización en slots). */
class BloqueDisponibilidadTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 1, 1, 8, 0);
    private static final LocalDate MANANA = AHORA.toLocalDate().plusDays(1);

    @Test
    void crear_conHorarioFuturoValido_creaElBloque() {
        BloqueDisponibilidad bloque = BloqueDisponibilidad.crear(1L, 1L, MANANA, LocalTime.of(8, 0),
            LocalTime.of(12, 0), AHORA);

        assertThat(bloque.getId()).isNull();
        assertThat(bloque.isActivo()).isTrue();
    }

    @Test
    void crear_enElPasado_lanzaValidacionNegocio() {
        LocalDate ayer = AHORA.toLocalDate().minusDays(1);

        assertThatThrownBy(() -> BloqueDisponibilidad.crear(1L, 1L, ayer, LocalTime.of(8, 0), LocalTime.of(12, 0), AHORA))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void crear_conDuracionNoMultiploDe30_lanzaValidacionNegocio() {
        assertThatThrownBy(() -> BloqueDisponibilidad.crear(1L, 1L, MANANA, LocalTime.of(8, 0), LocalTime.of(8, 45), AHORA))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void crear_conHoraFinAntesQueHoraInicio_lanzaValidacionNegocio() {
        assertThatThrownBy(() -> BloqueDisponibilidad.crear(1L, 1L, MANANA, LocalTime.of(12, 0), LocalTime.of(8, 0), AHORA))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void generarSlots_discretizaEnBloquesDe30Minutos() {
        BloqueDisponibilidad bloque = BloqueDisponibilidad.crear(1L, 1L, MANANA, LocalTime.of(8, 0),
            LocalTime.of(9, 0), AHORA);

        var slots = bloque.generarSlots();

        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).inicio()).isEqualTo(LocalDateTime.of(MANANA, LocalTime.of(8, 0)));
        assertThat(slots.get(0).fin()).isEqualTo(LocalDateTime.of(MANANA, LocalTime.of(8, 30)));
        assertThat(slots.get(1).inicio()).isEqualTo(slots.get(0).fin());
        assertThat(slots.get(1).fin()).isEqualTo(LocalDateTime.of(MANANA, LocalTime.of(9, 0)));
    }

    @Test
    void seSolapaCon_bloquesQueSeCruzanElMismoDia_devuelveTrue() {
        BloqueDisponibilidad a = BloqueDisponibilidad.crear(1L, 1L, MANANA, LocalTime.of(8, 0), LocalTime.of(10, 0), AHORA);
        BloqueDisponibilidad b = BloqueDisponibilidad.crear(1L, 1L, MANANA, LocalTime.of(9, 0), LocalTime.of(11, 0), AHORA);

        assertThat(a.seSolapaCon(b)).isTrue();
    }

    @Test
    void seSolapaCon_bloquesConsecutivosSinCruce_devuelveFalse() {
        BloqueDisponibilidad a = BloqueDisponibilidad.crear(1L, 1L, MANANA, LocalTime.of(8, 0), LocalTime.of(10, 0), AHORA);
        BloqueDisponibilidad b = BloqueDisponibilidad.crear(1L, 1L, MANANA, LocalTime.of(10, 0), LocalTime.of(12, 0), AHORA);

        assertThat(a.seSolapaCon(b)).isFalse();
    }

    @Test
    void seSolapaCon_diasDistintos_devuelveFalse() {
        BloqueDisponibilidad a = BloqueDisponibilidad.crear(1L, 1L, MANANA, LocalTime.of(8, 0), LocalTime.of(10, 0), AHORA);
        BloqueDisponibilidad b = BloqueDisponibilidad.crear(1L, 1L, MANANA.plusDays(1), LocalTime.of(8, 0),
            LocalTime.of(10, 0), AHORA);

        assertThat(a.seSolapaCon(b)).isFalse();
    }
}
