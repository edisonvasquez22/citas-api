package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand;
import com.fcv.citas.application.port.in.ConsultarDisponibilidadUseCase.Consulta;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.BloqueDisponibilidad;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.testsupport.InMemoryDisponibilidadStore;
import com.fcv.citas.testsupport.InMemoryEspecialidadRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import com.fcv.citas.testsupport.InMemorySlotRepositoryAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-013: CA-01 (filtro combinado) y CA-02 (60 min sin par consecutivo libre se excluye). */
class ConsultarDisponibilidadServiceTest {

    private static final Long SEDE = 1L;
    private static final LocalDate FECHA = LocalDate.now().plusDays(1);

    private ConsultarDisponibilidadService service;
    private InMemoryDisponibilidadStore store;
    private Long especialidadGeneralId;
    private Long especialidad60MinId;
    private Long profesionalId;

    @BeforeEach
    void setUp() {
        InMemoryEspecialidadRepositoryAdapter especialidadRepository = new InMemoryEspecialidadRepositoryAdapter();
        AdministrarEspecialidadesService especialidadesService = new AdministrarEspecialidadesService(especialidadRepository);
        especialidadGeneralId = especialidadesService.crear(new CrearCommand("MEDICINA_GENERAL", "Medicina General",
            30, true, false)).id();
        especialidad60MinId = especialidadesService.crear(new CrearCommand("NEURO", "Neurología", 60, false, true)).id();

        InMemoryProfesionalRepositoryAdapter profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        Profesional guardado = profesionalRepository.guardar(Profesional.registrar(100L, "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(especialidadGeneralId, true),
                new AsignacionEspecialidad(especialidad60MinId, false)),
            Set.of(SEDE)));
        profesionalId = guardado.getId();

        store = new InMemoryDisponibilidadStore();
        store.crearBloque(BloqueDisponibilidad.crear(profesionalId, SEDE, FECHA, LocalTime.of(8, 0),
            LocalTime.of(9, 0), LocalDateTime.now()));

        service = new ConsultarDisponibilidadService(especialidadRepository, profesionalRepository,
            new InMemorySlotRepositoryAdapter(store));
    }

    @Test
    void consultar_especialidad30Min_devuelveCadaSlotSueltoComoVentana() {
        var resultado = service.consultar(new Consulta(especialidadGeneralId, FECHA, SEDE, null));

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).inicio()).isEqualTo(LocalDateTime.of(FECHA, LocalTime.of(8, 0)));
        assertThat(resultado.get(1).inicio()).isEqualTo(LocalDateTime.of(FECHA, LocalTime.of(8, 30)));
    }

    @Test
    void consultar_especialidad60Min_conAmbosSlotsLibres_devuelveUnaVentana() {
        var resultado = service.consultar(new Consulta(especialidad60MinId, FECHA, SEDE, null));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).inicio()).isEqualTo(LocalDateTime.of(FECHA, LocalTime.of(8, 0)));
        assertThat(resultado.get(0).fin()).isEqualTo(LocalDateTime.of(FECHA, LocalTime.of(9, 0)));
    }

    @Test
    void consultar_especialidad60Min_conSoloUnSlotSueltoLibre_noOfreceEsaVentana() {
        var slot = store.buscarSlotsConsecutivosLibres(profesionalId, SEDE, LocalDateTime.of(FECHA, LocalTime.of(8, 30)), 1)
            .orElseThrow();
        store.reservarAtomicamente(slot, 999L);

        var resultado = service.consultar(new Consulta(especialidad60MinId, FECHA, SEDE, null));

        assertThat(resultado).isEmpty();
    }
}
