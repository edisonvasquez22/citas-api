package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.FuenteCambioEstado;
import com.fcv.citas.domain.model.SolicitudReprogramacion;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import com.fcv.citas.testsupport.InMemoryCitaRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryHistorialEstadoCitaAdapter;
import com.fcv.citas.testsupport.InMemorySolicitudReprogramacionRepositoryAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * HU-017: CA-01 (listado propio con filtros, incluido el motivo de rechazo cuando exista) y el desenlace de la
 * última solicitud de reprogramación de cada cita (HU-019/HU-020, LOOP_02 — ver
 * `docs/wiki/scrum/historias-de-usuario/HU-019-solicitar-reprogramacion.md`).
 */
class ConsultarMisCitasServiceTest {

    private InMemoryCitaRepositoryAdapter citaRepository;
    private InMemoryHistorialEstadoCitaAdapter historialAdapter;
    private InMemorySolicitudReprogramacionRepositoryAdapter solicitudReprogramacionAdapter;
    private ConsultarMisCitasService service;

    private static final Long PACIENTE_A = 1L;
    private static final Long PACIENTE_B = 2L;

    @BeforeEach
    void setUp() {
        citaRepository = new InMemoryCitaRepositoryAdapter();
        historialAdapter = new InMemoryHistorialEstadoCitaAdapter();
        solicitudReprogramacionAdapter = new InMemorySolicitudReprogramacionRepositoryAdapter();
        service = new ConsultarMisCitasService(citaRepository, historialAdapter, solicitudReprogramacionAdapter);
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

    @Test
    void listar_sinSolicitudDeReprogramacion_reprogramacionEsNull() {
        Cita cita = guardarGeneral(PACIENTE_A, LocalDateTime.of(2027, 1, 10, 8, 0));

        var resultado = service.listar(PACIENTE_A, null, null);

        var encontrada = resultado.stream().filter(r -> r.citaId().equals(cita.getId())).findFirst();
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().reprogramacion()).isNull();
    }

    @Test
    void listar_conSolicitudDeReprogramacionPendiente_incluyeSuEstadoYNuevoHorario() {
        Cita cita = guardarGeneral(PACIENTE_A, LocalDateTime.of(2027, 1, 10, 8, 0));
        LocalDateTime nuevoInicio = LocalDateTime.of(2027, 1, 20, 9, 0);
        SolicitudReprogramacion solicitud = solicitudReprogramacionAdapter.guardar(
            SolicitudReprogramacion.solicitar(cita.getId(), PACIENTE_A, 1L, cita.getInicio(), cita.getFin(),
                nuevoInicio, nuevoInicio.plusMinutes(30)));

        var resultado = service.listar(PACIENTE_A, null, null);

        var encontrada = resultado.stream().filter(r -> r.citaId().equals(cita.getId())).findFirst();
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().reprogramacion()).isNotNull();
        assertThat(encontrada.get().reprogramacion().solicitudId()).isEqualTo(solicitud.getId());
        assertThat(encontrada.get().reprogramacion().estado()).isEqualTo("PENDING");
        assertThat(encontrada.get().reprogramacion().inicioSolicitado()).isEqualTo(nuevoInicio);
        assertThat(encontrada.get().reprogramacion().motivoDecision()).isNull();
    }

    @Test
    void listar_conSolicitudDeReprogramacionRechazada_incluyeElMotivo() {
        Cita cita = guardarGeneral(PACIENTE_A, LocalDateTime.of(2027, 1, 10, 8, 0));
        LocalDateTime nuevoInicio = LocalDateTime.of(2027, 1, 20, 9, 0);
        SolicitudReprogramacion pendiente = solicitudReprogramacionAdapter.guardar(
            SolicitudReprogramacion.solicitar(cita.getId(), PACIENTE_A, 1L, cita.getInicio(), cita.getFin(),
                nuevoInicio, nuevoInicio.plusMinutes(30)));
        solicitudReprogramacionAdapter.guardar(
            pendiente.rechazar(99L, "El nuevo horario ya no está disponible", LocalDateTime.of(2027, 1, 11, 10, 0)));

        var resultado = service.listar(PACIENTE_A, null, null);

        var encontrada = resultado.stream().filter(r -> r.citaId().equals(cita.getId())).findFirst();
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().reprogramacion().estado()).isEqualTo("REJECTED");
        assertThat(encontrada.get().reprogramacion().motivoDecision()).isEqualTo("El nuevo horario ya no está disponible");
    }
}
