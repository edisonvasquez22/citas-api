package com.fcv.citas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.domain.exception.TransicionEstadoInvalidaException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** HU-014/HU-015/HU-016: ciclo de vida de la cita (RN-02, RN-03, RN-04). */
class CitaTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 1, 2, 8, 0);
    private static final LocalDateTime FIN = INICIO.plusMinutes(30);

    @Test
    void solicitarGeneral_naceEnApproved() {
        Cita cita = Cita.solicitarGeneral(1L, 2L, 3L, 4L, "control", INICIO, FIN);

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.APPROVED);
        assertThat(cita.getId()).isNull();
    }

    @Test
    void solicitarEspecializada_naceEnRequested() {
        Cita cita = Cita.solicitarEspecializada(1L, 2L, 3L, 4L, "control", INICIO, FIN);

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.REQUESTED);
    }

    @Test
    void aprobar_sobreRequested_pasaAApproved() {
        Cita requested = Cita.reconstruir(10L, 1L, 2L, 3L, 4L, EstadoCita.REQUESTED, null, INICIO, FIN, 1L, null,
            null, null);

        Cita aprobada = requested.aprobar(99L, LocalDateTime.now());

        assertThat(aprobada.getEstado()).isEqualTo(EstadoCita.APPROVED);
        assertThat(aprobada.getAprobadoPorUsuarioId()).isEqualTo(99L);
    }

    @Test
    void aprobar_sobreUnaCitaQueNoEstaRequested_lanzaTransicionInvalida() {
        Cita aprobada = Cita.reconstruir(10L, 1L, 2L, 3L, 4L, EstadoCita.APPROVED, null, INICIO, FIN, 1L, 5L,
            LocalDateTime.now(), null);

        assertThatThrownBy(() -> aprobada.aprobar(99L, LocalDateTime.now()))
            .isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    void rechazar_conMotivo_pasaARejectedYGuardaElMotivo() {
        Cita requested = Cita.reconstruir(10L, 1L, 2L, 3L, 4L, EstadoCita.REQUESTED, null, INICIO, FIN, 1L, null,
            null, null);

        Cita rechazada = requested.rechazar(99L, "sin cupo", LocalDateTime.now());

        assertThat(rechazada.getEstado()).isEqualTo(EstadoCita.REJECTED);
        assertThat(rechazada.getMotivoDecision()).isEqualTo("sin cupo");
    }

    @Test
    void rechazar_sinMotivo_lanzaValidacionNegocio() {
        Cita requested = Cita.reconstruir(10L, 1L, 2L, 3L, 4L, EstadoCita.REQUESTED, null, INICIO, FIN, 1L, null,
            null, null);

        assertThatThrownBy(() -> requested.rechazar(99L, "  ", LocalDateTime.now()))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void rechazar_sobreUnaCitaQueNoEstaRequested_lanzaTransicionInvalida() {
        Cita rechazada = Cita.reconstruir(10L, 1L, 2L, 3L, 4L, EstadoCita.REJECTED, null, INICIO, FIN, 1L, 5L,
            LocalDateTime.now(), "otro motivo");

        assertThatThrownBy(() -> rechazada.rechazar(99L, "motivo nuevo", LocalDateTime.now()))
            .isInstanceOf(TransicionEstadoInvalidaException.class);
    }
}
