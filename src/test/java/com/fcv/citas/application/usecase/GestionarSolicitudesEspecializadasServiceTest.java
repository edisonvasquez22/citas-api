package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand;
import com.fcv.citas.application.port.in.SolicitarCitaEspecializadaUseCase;
import com.fcv.citas.domain.exception.TransicionEstadoInvalidaException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-016: CA-01 a CA-04. */
class GestionarSolicitudesEspecializadasServiceTest {

    private static final Long SEDE = 1L;
    private static final LocalDate FECHA = LocalDate.now().plusDays(1);
    private static final Long ADMIN_ID = 999L;

    private SolicitarCitaEspecializadaService solicitarService;
    private GestionarSolicitudesEspecializadasService gestionarService;
    private InMemoryDisponibilidadStore store;
    private Long especialidadId;
    private Long profesionalId;

    @BeforeEach
    void setUp() {
        InMemoryEspecialidadRepositoryAdapter especialidadRepository = new InMemoryEspecialidadRepositoryAdapter();
        especialidadId = new AdministrarEspecialidadesService(especialidadRepository)
            .crear(new CrearCommand("NEURO", "Neurología", 30, false, true)).id();

        InMemoryProfesionalRepositoryAdapter profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        profesionalId = profesionalRepository.guardar(Profesional.registrar(100L, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(especialidadId, true)), Set.of(SEDE))).getId();

        store = new InMemoryDisponibilidadStore();
        store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, FECHA, LocalTime.of(8, 0),
            LocalTime.of(8, 30), LocalDateTime.now()));

        InMemorySlotRepositoryAdapter slotRepository = new InMemorySlotRepositoryAdapter(store);
        InMemoryCitaRepositoryAdapter citaRepository = new InMemoryCitaRepositoryAdapter();
        InMemoryHistorialEstadoCitaAdapter historial = new InMemoryHistorialEstadoCitaAdapter();

        solicitarService = new SolicitarCitaEspecializadaService(especialidadRepository, profesionalRepository,
            slotRepository, citaRepository, historial);
        gestionarService = new GestionarSolicitudesEspecializadasService(citaRepository, slotRepository, historial);
    }

    private Long crearSolicitud() {
        return solicitarService.solicitar(new SolicitarCitaEspecializadaUseCase.Command(1L, profesionalId, SEDE,
            especialidadId, "control", FECHA, LocalTime.of(8, 0))).citaId();
    }

    @Test
    void aprobar_solicitudRequested_pasaAApproved() {
        Long citaId = crearSolicitud();

        var resumen = gestionarService.aprobar(ADMIN_ID, citaId);

        assertThat(resumen.estado()).isEqualTo(EstadoCita.APPROVED.name());
    }

    @Test
    void rechazar_conMotivo_pasaARejectedYLiberaElHorario() {
        Long citaId = crearSolicitud();

        var resumen = gestionarService.rechazar(ADMIN_ID, citaId, "sin cupo disponible");

        assertThat(resumen.estado()).isEqualTo(EstadoCita.REJECTED.name());
        assertThat(resumen.motivoDecision()).isEqualTo("sin cupo disponible");
        // El horario liberado vuelve a poder solicitarse (RN-09).
        Long segundaSolicitud = crearSolicitud();
        assertThat(segundaSolicitud).isNotNull();
    }

    @Test
    void rechazar_sinMotivo_seRechaza() {
        Long citaId = crearSolicitud();

        assertThatThrownBy(() -> gestionarService.rechazar(ADMIN_ID, citaId, "  "))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void decidir_unaCitaQueYaNoEstaRequested_lanzaTransicionInvalida() {
        Long citaId = crearSolicitud();
        gestionarService.aprobar(ADMIN_ID, citaId);

        assertThatThrownBy(() -> gestionarService.aprobar(ADMIN_ID, citaId))
            .isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    void listarSolicitudes_filtraPorSedeYSoloDevuelveRequested() {
        Long citaId = crearSolicitud();

        assertThat(gestionarService.listarSolicitudes(SEDE, null, null, null)).hasSize(1);
        assertThat(gestionarService.listarSolicitudes(SEDE + 1, null, null, null)).isEmpty();

        gestionarService.aprobar(ADMIN_ID, citaId);

        assertThat(gestionarService.listarSolicitudes(SEDE, null, null, null)).isEmpty();
    }
}
