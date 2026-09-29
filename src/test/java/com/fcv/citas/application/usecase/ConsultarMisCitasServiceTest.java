package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.FuenteCambioEstado;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import com.fcv.citas.testsupport.InMemoryCitaRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryHistorialEstadoCitaAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-017: CA-01 (listado propio con filtros, incluido el motivo de rechazo cuando exista). */
class ConsultarMisCitasServiceTest {

    private InMemoryCitaRepositoryAdapter citaRepository;
    private InMemoryHistorialEstadoCitaAdapter historialAdapter;
    private ConsultarMisCitasService service;

    private static final Long PACIENTE_A = 1L;
    private static final Long PACIENTE_B = 2L;

    @BeforeEach
    void setUp() {
        citaRepository = new InMemoryCitaRepositoryAdapter();
        historialAdapter = new InMemoryHistorialEstadoCitaAdapter();
        service = new ConsultarMisCitasService(citaRepository, historialAdapter);
    }

    private Cita guardarGeneral(Long pacienteId, LocalDateTime inicio) {
        return citaRepository.guardar(
            Cita.solicitarGeneral(pacienteId, 10L, 1L, 1L, null, inicio, inicio.plusMinutes(30)));
    }

    @Test
    void listar_devuelveSoloCitasPropias() {
        guardarGeneral(PACIENTE_A, LocalDateTime.of(2027, 1, 10, 8, 0));
        guardarGeneral(PACIENTE_B, LocalDateTime.of(2027, 1, 10, 9, 0));

        var resultado = service.listar(PACIENTE_A, null, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).sedeId()).isEqualTo(1L);
    }

    @Test
    void listar_filtraPorEstado() {
        Cita aprobada = guardarGeneral(PACIENTE_A, LocalDateTime.of(2027, 1, 10, 8, 0));
        Cita especializada = citaRepository.guardar(Cita.solicitarEspecializada(PACIENTE_A, 10L, 1L, 2L, null,
            LocalDateTime.of(2027, 1, 11, 8, 0), LocalDateTime.of(2027, 1, 11, 9, 0)));

        var soloRequested = service.listar(PACIENTE_A, EstadoCita.REQUESTED, null);

        assertThat(soloRequested).hasSize(1);
        assertThat(soloRequested.get(0).citaId()).isEqualTo(especializada.getId());
        assertThat(aprobada.getEstado()).isEqualTo(EstadoCita.APPROVED);
    }

    @Test
    void listar_filtraPorFecha() {
        guardarGeneral(PACIENTE_A, LocalDateTime.of(2027, 1, 10, 8, 0));
        guardarGeneral(PACIENTE_A, LocalDateTime.of(2027, 1, 15, 8, 0));

        var resultado = service.listar(PACIENTE_A, null, LocalDate.of(2027, 1, 15));

        assertThat(resultado).hasSize(1);
    }

    @Test
    void listar_incluyeMotivoDeRechazoCuandoExiste() {
        Cita rechazada = citaRepository.guardar(Cita.solicitarEspecializada(PACIENTE_A, 10L, 1L, 2L, null,
            LocalDateTime.of(2027, 1, 10, 8, 0), LocalDateTime.of(2027, 1, 10, 9, 0)));
        historialAdapter.registrar(TransicionEstadoCita.nueva(rechazada.getId(), EstadoCita.REQUESTED, PACIENTE_A,
            FuenteCambioEstado.SYSTEM, null, LocalDateTime.of(2027, 1, 9, 10, 0)));
        historialAdapter.registrar(TransicionEstadoCita.nueva(rechazada.getId(), EstadoCita.REJECTED, 99L,
            FuenteCambioEstado.ADMIN, "Cupo no disponible", LocalDateTime.of(2027, 1, 9, 11, 0)));
        Cita guardadaComoRechazada = citaRepository.guardar(rechazada.rechazar(99L, "Cupo no disponible",
            LocalDateTime.of(2027, 1, 9, 11, 0)));

        var resultado = service.listar(PACIENTE_A, null, null);

        var encontrada = resultado.stream().filter(r -> r.citaId().equals(guardadaComoRechazada.getId())).findFirst();
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().motivoDecision()).isEqualTo("Cupo no disponible");
    }
}
