package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand;
import com.fcv.citas.application.port.in.SolicitarCitaEspecializadaUseCase.Command;
import com.fcv.citas.domain.exception.HorarioNoDisponibleException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
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

/** HU-015: CA-01 a CA-03 (misma prueba de doble reserva que HU-014, sobre la especialidad). */
class SolicitarCitaEspecializadaServiceTest {

    private static final Long SEDE = 1L;
    private static final LocalDate FECHA = LocalDate.now().plusDays(1);
    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);

    private InMemoryEspecialidadRepositoryAdapter especialidadRepository;
    private AdministrarEspecialidadesService especialidadesService;
    private SolicitarCitaEspecializadaService service;
    private Long especialidadId;
    private Long profesionalId;

    @BeforeEach
    void setUp() {
        especialidadRepository = new InMemoryEspecialidadRepositoryAdapter();
        especialidadesService = new AdministrarEspecialidadesService(especialidadRepository);
        especialidadId = especialidadesService.crear(new CrearCommand("NEURO", "Neurología", 30, false, true)).id();

        InMemoryProfesionalRepositoryAdapter profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        profesionalId = profesionalRepository.guardar(Profesional.registrar(100L, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(especialidadId, true)), Set.of(SEDE))).getId();

        InMemoryDisponibilidadStore store = new InMemoryDisponibilidadStore();
        store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, FECHA, HORA_INICIO, HORA_INICIO.plusMinutes(30),
            LocalDateTime.now()));

        service = new SolicitarCitaEspecializadaService(especialidadRepository, profesionalRepository,
            new InMemorySlotRepositoryAdapter(store), new InMemoryCitaRepositoryAdapter(),
            new InMemoryHistorialEstadoCitaAdapter());
    }

    private Command comando(Long especialidad) {
        return new Command(1L, profesionalId, SEDE, especialidad, "control", FECHA, HORA_INICIO);
    }

    @Test
    void solicitar_conEspecialidadAsociadaYHorarioLibre_naceEnRequested() {
        var resultado = service.solicitar(comando(especialidadId));

        assertThat(resultado.estado()).isEqualTo(EstadoCita.REQUESTED.name());
    }

    @Test
    void solicitar_conEspecialidadNoAsociadaAlProfesional_seRechaza() {
        Long otraEspecialidad = especialidadesService.crear(new CrearCommand("UROLOGIA", "Urología", 30, false, true)).id();

        assertThatThrownBy(() -> service.solicitar(comando(otraEspecialidad)))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void solicitar_conEspecialidadInactiva_seRechaza() {
        especialidadesService.cambiarEstado(especialidadId, false);

        assertThatThrownBy(() -> service.solicitar(comando(especialidadId)))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void solicitar_bajoConcurrencia_soloUnaSolicitudRetieneElHorario() throws InterruptedException {
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
                    service.solicitar(comando(especialidadId));
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
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        assertThat(exitos.get()).isEqualTo(1);
        assertThat(rechazos.get()).isEqualTo(hilos - 1);
    }
}
