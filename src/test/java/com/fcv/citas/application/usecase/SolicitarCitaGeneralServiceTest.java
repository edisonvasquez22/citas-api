package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand;
import com.fcv.citas.application.port.in.SolicitarCitaGeneralUseCase.Command;
import com.fcv.citas.domain.exception.HorarioNoDisponibleException;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.BloqueDisponibilidad;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.testsupport.InMemoryCitaRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryDisponibilidadStore;
import com.fcv.citas.testsupport.InMemoryEspecialidadRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryHistorialEstadoCitaAdapter;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import com.fcv.citas.testsupport.InMemorySlotRepositoryAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * HU-014: CA-01 a CA-03. CA-03 es la prueba de doble reserva bajo concurrencia
 * real pedida explícitamente por GUIA_SESIONES_S2_S6.md para S3.
 */
class SolicitarCitaGeneralServiceTest {

    private static final Long SEDE = 1L;
    private static final LocalDate FECHA = LocalDate.now().plusDays(1);
    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);

    private SolicitarCitaGeneralService service;
    private Long especialidadGeneralId;
    private Long profesionalId;
    private InMemoryHistorialEstadoCitaAdapter historial;

    @BeforeEach
    void setUp() {
        InMemoryEspecialidadRepositoryAdapter especialidadRepository = new InMemoryEspecialidadRepositoryAdapter();
        especialidadGeneralId = new AdministrarEspecialidadesService(especialidadRepository)
            .crear(new CrearCommand("MEDICINA_GENERAL", "Medicina General", 30, true, false)).id();

        InMemoryProfesionalRepositoryAdapter profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        profesionalId = profesionalRepository.guardar(Profesional.registrar(100L, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(especialidadGeneralId, true)), Set.of(SEDE))).getId();

        InMemoryDisponibilidadStore store = new InMemoryDisponibilidadStore();
        store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, FECHA, HORA_INICIO, HORA_INICIO.plusMinutes(30),
            LocalDateTime.now()));

        historial = new InMemoryHistorialEstadoCitaAdapter();
        service = new SolicitarCitaGeneralService(especialidadRepository, profesionalRepository,
            new InMemorySlotRepositoryAdapter(store), new InMemoryCitaRepositoryAdapter(), historial);
    }

    private Command comando() {
        return new Command(1L, profesionalId, SEDE, especialidadGeneralId, "control", FECHA, HORA_INICIO);
    }

    @Test
    void solicitar_horarioDisponible_quedaApprovedYAuditado() {
        var resultado = service.solicitar(comando());

        assertThat(resultado.estado()).isEqualTo(EstadoCita.APPROVED.name());
        assertThat(historial.listarPorCita(resultado.citaId())).hasSize(1);
        assertThat(historial.listarPorCita(resultado.citaId()).get(0).estado()).isEqualTo(EstadoCita.APPROVED);
    }

    @Test
    void solicitar_horarioYaTomado_lanzaHorarioNoDisponible() {
        service.solicitar(comando());

        assertThatThrownBy(() -> service.solicitar(comando())).isInstanceOf(HorarioNoDisponibleException.class);
    }

    @Test
    void solicitar_bajoConcurrencia_soloUnaSolicitudGanaElHorario() throws InterruptedException {
        int hilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        CountDownLatch listos = new CountDownLatch(hilos);
        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger rechazos = new AtomicInteger(0);

        for (int i = 0; i < hilos; i++) {
            executor.submit(() -> {
                listos.countDown();
                try {
                    salida.await();
                    service.solicitar(comando());
                    exitos.incrementAndGet();
                } catch (HorarioNoDisponibleException e) {
                    rechazos.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        listos.await(5, TimeUnit.SECONDS);
        salida.countDown();
        executor.shutdown();
        boolean terminado = executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(terminado).isTrue();
        assertThat(exitos.get()).isEqualTo(1);
        assertThat(rechazos.get()).isEqualTo(hilos - 1);
    }
}
