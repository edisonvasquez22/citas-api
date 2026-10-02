package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.SolicitarReprogramacionUseCase.Command;
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
import com.fcv.citas.application.port.out.SolicitudReprogramacionRepositoryPort;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-020: CA-01 a CA-03 (aprobación, rechazo con motivo, rechazo sin motivo). */
class GestionarReprogramacionesServiceTest {

    private static final Long SEDE = 1L;
    private static final Long PACIENTE = 1L;
    private static final Long ADMIN = 999L;

    private InMemoryCitaRepositoryAdapter citaRepository;
    private InMemorySlotRepositoryAdapter slotRepository;
    private InMemorySolicitudReprogramacionRepositoryAdapter solicitudRepository;
    private SolicitarReprogramacionService solicitarService;
    private GestionarReprogramacionesService gestionarService;
    private Long profesionalId;
    private Cita citaOriginal;
    private LocalDateTime inicioOriginal;
    private LocalDate fechaNueva;
    private LocalTime horaNueva;

    @BeforeEach
    void setUp() {
        InMemoryProfesionalRepositoryAdapter profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        profesionalId = profesionalRepository.guardar(Profesional.registrar(100L, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(1L, true)), Set.of(SEDE))).getId();

        InMemoryDisponibilidadStore store = new InMemoryDisponibilidadStore();
        LocalDate fechaOriginal = LocalDate.now().plusDays(1);
        LocalTime horaOriginal = LocalTime.of(8, 0);
        fechaNueva = LocalDate.now().plusDays(2);
        horaNueva = LocalTime.of(9, 0);
        store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, fechaOriginal, horaOriginal,
            horaOriginal.plusMinutes(30), LocalDateTime.now()));
        store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, fechaNueva, horaNueva,
            horaNueva.plusMinutes(30), LocalDateTime.now()));

        slotRepository = new InMemorySlotRepositoryAdapter(store);
        citaRepository = new InMemoryCitaRepositoryAdapter();
        solicitudRepository = new InMemorySolicitudReprogramacionRepositoryAdapter();
        solicitarService = new SolicitarReprogramacionService(citaRepository, profesionalRepository, slotRepository,
            solicitudRepository);
        gestionarService = new GestionarReprogramacionesService(solicitudRepository, citaRepository, slotRepository,
            evento -> { });

        inicioOriginal = LocalDateTime.of(fechaOriginal, horaOriginal);
        citaOriginal = citaRepository.guardar(Cita.solicitarGeneral(PACIENTE, profesionalId, SEDE, 1L, null,
            inicioOriginal, inicioOriginal.plusMinutes(30)));
        var slotsOriginales = store.buscarSlotsConsecutivosLibres(profesionalId, SEDE, inicioOriginal, 1).orElseThrow();
        store.reservarAtomicamente(slotsOriginales, citaOriginal.getId());
    }

    private Long solicitarReprogramacion() {
        return solicitarService.solicitar(new Command(PACIENTE, citaOriginal.getId(), SEDE, fechaNueva, horaNueva))
            .solicitudId();
    }

    @Test
    void aprobar_liberaFranjaAntiguaYActualizaLaCitaConLaNueva() {
        Long solicitudId = solicitarReprogramacion();
        LocalDateTime nuevoInicio = LocalDateTime.of(fechaNueva, horaNueva);

        var resultado = gestionarService.aprobar(ADMIN, solicitudId);

        assertThat(resultado.estado()).isEqualTo("APPROVED");
        var citaActualizada = citaRepository.buscarPorId(citaOriginal.getId()).orElseThrow();
        assertThat(citaActualizada.getInicio()).isEqualTo(nuevoInicio);
        assertThat(citaActualizada.getEstado()).isEqualTo(EstadoCita.APPROVED);

        // La franja antigua quedó libre: se puede volver a reservar para otra cita.
        assertThat(slotRepository.buscarSlotsConsecutivosLibres(profesionalId, SEDE, inicioOriginal, 1)).isPresent();
    }

    @Test
    void rechazar_conMotivo_liberaSoloLaFranjaNuevaYDejaLaCitaOriginalIntacta() {
        Long solicitudId = solicitarReprogramacion();

        var resultado = gestionarService.rechazar(ADMIN, solicitudId, "Cupo no disponible");

        assertThat(resultado.estado()).isEqualTo("REJECTED");
        assertThat(resultado.motivoDecision()).isEqualTo("Cupo no disponible");
        var citaTrasRechazo = citaRepository.buscarPorId(citaOriginal.getId()).orElseThrow();
        assertThat(citaTrasRechazo.getInicio()).isEqualTo(inicioOriginal);

        // La franja nueva quedó libre; la antigua sigue ocupada por la cita original.
        LocalDateTime inicioNuevo = LocalDateTime.of(fechaNueva, horaNueva);
        assertThat(slotRepository.buscarSlotsConsecutivosLibres(profesionalId, SEDE, inicioNuevo, 1)).isPresent();
        assertThat(slotRepository.buscarSlotsConsecutivosLibres(profesionalId, SEDE, inicioOriginal, 1)).isEmpty();
    }

    @Test
    void rechazar_sinMotivo_seRechaza() {
        Long solicitudId = solicitarReprogramacion();

        assertThatThrownBy(() -> gestionarService.rechazar(ADMIN, solicitudId, "  "))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    /** RF-18: la bandeja PENDING se filtra por sede, profesional, especialidad y fecha solicitadas. */
    @Test
    void listarPendientes_aplicaFiltrosOpcionales() {
        solicitarReprogramacion();

        assertThat(gestionarService.listarPendientes(null, null, null, null)).hasSize(1);
        assertThat(gestionarService.listarPendientes(SEDE, profesionalId, 1L, fechaNueva)).hasSize(1);
        assertThat(gestionarService.listarPendientes(SEDE + 1, null, null, null)).isEmpty();
        assertThat(gestionarService.listarPendientes(null, profesionalId + 1, null, null)).isEmpty();
        assertThat(gestionarService.listarPendientes(null, null, 2L, null)).isEmpty();
        assertThat(gestionarService.listarPendientes(null, null, null, fechaNueva.plusDays(1))).isEmpty();
    }

    /** HU-020 (concurrencia): 10 admins aprueban la MISMA solicitud a la vez; solo uno debe tener éxito. */
    @Test
    void aprobar_concurrentementeLaMismaSolicitud_soloUnaAprobacionTieneExito() throws Exception {
        Long solicitudId = solicitarReprogramacion();
        int hilos = 10;
        // Latencia simulada al leer la solicitud (como una BD real): ensancha la ventana de carrera.
        var solicitudesLentas = (SolicitudReprogramacionRepositoryPort) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[] {SolicitudReprogramacionRepositoryPort.class},
            (proxy, metodo, args) -> {
                Object r = metodo.invoke(solicitudRepository, args);
                if (metodo.getName().equals("buscarPorId")) {
                    Thread.sleep(50);
                }
                return r;
            });
        var concurrente = new GestionarReprogramacionesService(solicitudesLentas, citaRepository, slotRepository,
            evento -> { });
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch listos = new CountDownLatch(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger rechazadasPorEstado = new AtomicInteger();
        List<Future<?>> futuros = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            Callable<Void> tarea = () -> {
                listos.countDown();
                salida.await();
                try {
                    concurrente.aprobar(ADMIN, solicitudId);
                    exitos.incrementAndGet();
                } catch (TransicionEstadoInvalidaException e) {
                    rechazadasPorEstado.incrementAndGet();
                }
                return null;
            };
            futuros.add(pool.submit(tarea));
        }
        listos.await(5, TimeUnit.SECONDS);
        salida.countDown();
        for (Future<?> f : futuros) {
            f.get(10, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertThat(exitos.get()).isEqualTo(1);
        assertThat(rechazadasPorEstado.get()).isEqualTo(hilos - 1);
        assertThat(citaRepository.buscarPorId(citaOriginal.getId()).orElseThrow().getInicio())
            .isEqualTo(LocalDateTime.of(fechaNueva, horaNueva));
    }
}
