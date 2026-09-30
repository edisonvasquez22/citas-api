package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.SolicitarReprogramacionUseCase.Command;
import com.fcv.citas.domain.exception.HorarioNoDisponibleException;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.TransicionEstadoInvalidaException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.BloqueDisponibilidad;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.testsupport.InMemoryCitaRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryDisponibilidadStore;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import com.fcv.citas.testsupport.InMemorySlotRepositoryAdapter;
import com.fcv.citas.testsupport.InMemorySolicitudReprogramacionRepositoryAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * HU-019: CA-01 a CA-03 (solicitud válida, cita no reprogramable, nuevo horario no disponible) y LOOP_03
 * (una cita no puede tener dos solicitudes de reprogramación PENDING a la vez, ni siquiera bajo
 * concurrencia real — ver `prompts/goal-loop/LOOP_03_RETO_INDEPENDIENTE.md`).
 */
class SolicitarReprogramacionServiceTest {

    private static final Long SEDE = 1L;
    private static final Long PACIENTE = 1L;

    private InMemoryCitaRepositoryAdapter citaRepository;
    private InMemoryDisponibilidadStore store;
    private InMemorySlotRepositoryAdapter slotRepository;
    private InMemorySolicitudReprogramacionRepositoryAdapter solicitudRepository;
    private SolicitarReprogramacionService service;
    private Long profesionalId;
    private Cita citaOriginal;
    private LocalDate fechaOriginal;
    private LocalTime horaOriginal;
    private LocalDate fechaNueva;
    private LocalTime horaNueva;

    @BeforeEach
    void setUp() {
        InMemoryProfesionalRepositoryAdapter profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        profesionalId = profesionalRepository.guardar(Profesional.registrar(100L, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(1L, true)), Set.of(SEDE))).getId();

        store = new InMemoryDisponibilidadStore();
        fechaOriginal = LocalDate.now().plusDays(1);
        horaOriginal = LocalTime.of(8, 0);
        fechaNueva = LocalDate.now().plusDays(2);
        horaNueva = LocalTime.of(9, 0);
        store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, fechaOriginal, horaOriginal,
            horaOriginal.plusMinutes(30), LocalDateTime.now()));
        store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, fechaNueva, horaNueva,
            horaNueva.plusMinutes(30), LocalDateTime.now()));

        slotRepository = new InMemorySlotRepositoryAdapter(store);
        citaRepository = new InMemoryCitaRepositoryAdapter();
        solicitudRepository = new InMemorySolicitudReprogramacionRepositoryAdapter();
        service = new SolicitarReprogramacionService(citaRepository, profesionalRepository, slotRepository,
            solicitudRepository);

        LocalDateTime inicioOriginal = LocalDateTime.of(fechaOriginal, horaOriginal);
        citaOriginal = citaRepository.guardar(Cita.solicitarGeneral(PACIENTE, profesionalId, SEDE, 1L, null,
            inicioOriginal, inicioOriginal.plusMinutes(30)));
        // Reserva el slot original tal como lo haría SolicitarCitaGeneralService.
        var slotsOriginales = store.buscarSlotsConsecutivosLibres(profesionalId, SEDE, inicioOriginal, 1).orElseThrow();
        store.reservarAtomicamente(slotsOriginales, citaOriginal.getId());
    }

    private Command comando() {
        return new Command(PACIENTE, citaOriginal.getId(), SEDE, fechaNueva, horaNueva);
    }

    @Test
    void solicitar_horarioDisponible_quedaPendingSinTocarLaCitaOriginal() {
        var resultado = service.solicitar(comando());

        assertThat(resultado.estado()).isEqualTo("PENDING");
        assertThat(citaRepository.buscarPorId(citaOriginal.getId())).get()
            .satisfies(c -> {
                assertThat(c.getEstado()).isEqualTo(EstadoCita.APPROVED);
                assertThat(c.getInicio()).isEqualTo(citaOriginal.getInicio());
            });
    }

    @Test
    void solicitar_citaNoAprobada_seRechaza() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(3);
        Cita citaRequested = citaRepository.guardar(Cita.solicitarEspecializada(PACIENTE, profesionalId, SEDE, 1L,
            null, inicio, inicio.plusMinutes(30)));

        assertThatThrownBy(() -> service.solicitar(new Command(PACIENTE, citaRequested.getId(), SEDE, fechaNueva, horaNueva)))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void solicitar_citaDeOtroUsuario_seRechazaComoNoEncontrada() {
        assertThatThrownBy(() -> service.solicitar(new Command(999L, citaOriginal.getId(), SEDE, fechaNueva, horaNueva)))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void solicitar_nuevoHorarioNoDisponible_seRechazaSinAfectarLaCitaOriginal() {
        // Ocupa el horario nuevo con otra cita antes de intentar reprogramar.
        LocalDateTime inicioNuevo = LocalDateTime.of(fechaNueva, horaNueva);
        Cita otra = citaRepository.guardar(
            Cita.solicitarGeneral(2L, profesionalId, SEDE, 1L, null, inicioNuevo, inicioNuevo.plusMinutes(30)));
        slotRepository.reservarAtomicamente(
            slotRepository.buscarSlotsConsecutivosLibres(profesionalId, SEDE, inicioNuevo, 1).orElseThrow(),
            otra.getId());

        assertThatThrownBy(() -> service.solicitar(comando())).isInstanceOf(HorarioNoDisponibleException.class);

        assertThat(citaRepository.buscarPorId(citaOriginal.getId())).get()
            .extracting(Cita::getInicio).isEqualTo(citaOriginal.getInicio());
    }

    @Test
    void solicitar_conSolicitudPendienteExistente_seRechaza() {
        service.solicitar(comando());

        LocalDate otraFechaNueva = LocalDate.now().plusDays(3);
        LocalTime otraHoraNueva = LocalTime.of(10, 0);
        store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, otraFechaNueva, otraHoraNueva,
            otraHoraNueva.plusMinutes(30), LocalDateTime.now()));

        assertThatThrownBy(
            () -> service.solicitar(new Command(PACIENTE, citaOriginal.getId(), SEDE, otraFechaNueva, otraHoraNueva)))
            .isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    void solicitar_dosSolicitudesConcurrentesSobreLaMismaCita_soloUnaQuedaPending() throws InterruptedException {
        // Cada hilo pide un horario NUEVO distinto (cada uno con su propio slot libre, sin conflicto entre
        // sí) para aislar la regla que se está probando (LOOP_03: una PENDING por cita) de RN-01 (retención
        // atómica de slots), que ya tiene su propia prueba de concurrencia dedicada en HU-014/HU-015.
        int hilos = 8;
        for (int i = 0; i < hilos; i++) {
            LocalDate fecha = LocalDate.now().plusDays(10 + i);
            LocalTime hora = LocalTime.of(8, 0);
            store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, fecha, hora, hora.plusMinutes(30),
                LocalDateTime.now()));
        }

        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        CountDownLatch listos = new CountDownLatch(hilos);
        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger rechazosPorPendiente = new AtomicInteger(0);
        List<Exception> otrosErrores = new CopyOnWriteArrayList<>();

        for (int i = 0; i < hilos; i++) {
            LocalDate fecha = LocalDate.now().plusDays(10 + i);
            LocalTime hora = LocalTime.of(8, 0);
            executor.submit(() -> {
                listos.countDown();
                try {
                    salida.await();
                    service.solicitar(new Command(PACIENTE, citaOriginal.getId(), SEDE, fecha, hora));
                    exitos.incrementAndGet();
                } catch (TransicionEstadoInvalidaException e) {
                    rechazosPorPendiente.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    otrosErrores.add(e);
                }
            });
        }

        listos.await(5, TimeUnit.SECONDS);
        salida.countDown();
        executor.shutdown();
        boolean terminado = executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(terminado).isTrue();
        assertThat(otrosErrores).isEmpty();
        assertThat(exitos.get()).isEqualTo(1);
        assertThat(rechazosPorPendiente.get()).isEqualTo(hilos - 1);
    }
}
