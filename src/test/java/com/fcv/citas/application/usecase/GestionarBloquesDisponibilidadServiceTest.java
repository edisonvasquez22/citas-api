package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.GestionarBloquesDisponibilidadUseCase.CrearCommand;
import com.fcv.citas.application.port.in.GestionarBloquesDisponibilidadUseCase.EditarCommand;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.testsupport.InMemoryBloqueDisponibilidadRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryDisponibilidadStore;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import com.fcv.citas.testsupport.InMemorySlotRepositoryAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-012: CA-01 a CA-05. */
class GestionarBloquesDisponibilidadServiceTest {

    private static final LocalDate MANANA = LocalDate.now().plusDays(1);
    private static final Long USUARIO_ID = 100L;
    private static final Long SEDE_HABILITADA = 1L;
    private static final Long SEDE_NO_HABILITADA = 2L;

    private GestionarBloquesDisponibilidadService service;
    private InMemoryProfesionalRepositoryAdapter profesionalRepository;
    private InMemoryDisponibilidadStore store;

    @BeforeEach
    void setUp() {
        profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        profesionalRepository.guardar(Profesional.registrar(USUARIO_ID, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(10L, true)), Set.of(SEDE_HABILITADA)));
        store = new InMemoryDisponibilidadStore();
        service = new GestionarBloquesDisponibilidadService(profesionalRepository,
            new InMemoryBloqueDisponibilidadRepositoryAdapter(store));
    }

    @Test
    void crear_bloqueValido_quedaCreado() {
        var resultado = service.crear(new CrearCommand(USUARIO_ID, SEDE_HABILITADA, MANANA, LocalTime.of(8, 0),
            LocalTime.of(9, 0)));

        assertThat(resultado.id()).isNotNull();
        assertThat(service.listarPropios(USUARIO_ID)).hasSize(1);
    }

    @Test
    void crear_enElPasado_seRechaza() {
        LocalDate ayer = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> service.crear(new CrearCommand(USUARIO_ID, SEDE_HABILITADA, ayer,
            LocalTime.of(8, 0), LocalTime.of(9, 0)))).isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void crear_solapadoConOtroBloquePropio_seRechaza() {
        service.crear(new CrearCommand(USUARIO_ID, SEDE_HABILITADA, MANANA, LocalTime.of(8, 0), LocalTime.of(10, 0)));

        assertThatThrownBy(() -> service.crear(new CrearCommand(USUARIO_ID, SEDE_HABILITADA, MANANA,
            LocalTime.of(9, 0), LocalTime.of(11, 0)))).isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void crear_enSedeNoHabilitada_seRechaza() {
        assertThatThrownBy(() -> service.crear(new CrearCommand(USUARIO_ID, SEDE_NO_HABILITADA, MANANA,
            LocalTime.of(8, 0), LocalTime.of(9, 0)))).isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void eliminar_bloqueSinCitasComprometidas_seElimina() {
        var creado = service.crear(new CrearCommand(USUARIO_ID, SEDE_HABILITADA, MANANA, LocalTime.of(8, 0),
            LocalTime.of(9, 0)));

        service.eliminar(USUARIO_ID, creado.id());

        assertThat(service.listarPropios(USUARIO_ID)).isEmpty();
    }

    @Test
    void eliminar_bloqueConCitaComprometida_seRechaza() {
        var creado = service.crear(new CrearCommand(USUARIO_ID, SEDE_HABILITADA, MANANA, LocalTime.of(8, 0),
            LocalTime.of(9, 0)));
        // Simula que una cita ya retuvo el primer slot del bloque (CA-05).
        List<Long> slotIds = store
            .buscarSlotsConsecutivosLibres(creado.profesionalId(), SEDE_HABILITADA,
                LocalDateTime.of(MANANA, LocalTime.of(8, 0)), 1)
            .orElseThrow();
        store.reservarAtomicamente(slotIds, 999L);

        assertThatThrownBy(() -> service.eliminar(USUARIO_ID, creado.id()))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void editar_bloqueConCitaComprometida_seRechaza() {
        var creado = service.crear(new CrearCommand(USUARIO_ID, SEDE_HABILITADA, MANANA, LocalTime.of(8, 0),
            LocalTime.of(9, 0)));
        List<Long> slotIds = store
            .buscarSlotsConsecutivosLibres(creado.profesionalId(), SEDE_HABILITADA,
                LocalDateTime.of(MANANA, LocalTime.of(8, 0)), 1)
            .orElseThrow();
        store.reservarAtomicamente(slotIds, 999L);

        assertThatThrownBy(() -> service.editar(new EditarCommand(USUARIO_ID, creado.id(), SEDE_HABILITADA, MANANA,
            LocalTime.of(10, 0), LocalTime.of(11, 0)))).isInstanceOf(ValidacionNegocioException.class);
    }
}
