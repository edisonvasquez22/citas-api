package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.SolicitarReprogramacionUseCase.Command;
import com.fcv.citas.domain.exception.HorarioNoDisponibleException;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
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
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-019: CA-01 a CA-03 (solicitud válida, cita no reprogramable, nuevo horario no disponible). */
class SolicitarReprogramacionServiceTest {

    private static final Long SEDE = 1L;
    private static final Long PACIENTE = 1L;

    private InMemoryCitaRepositoryAdapter citaRepository;
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

        InMemoryDisponibilidadStore store = new InMemoryDisponibilidadStore();
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
}
