package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand;
import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase.EspecialidadAsignadaCommand;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.BloqueDisponibilidad;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.testsupport.InMemoryBloqueDisponibilidadRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryCitaRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryDisponibilidadStore;
import com.fcv.citas.testsupport.InMemoryEspecialidadRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryLocationRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActualizarAsignacionesProfesionalServiceTest {

    private ActualizarAsignacionesProfesionalService service;
    private InMemoryProfesionalRepositoryAdapter profesionalRepository;
    private InMemoryBloqueDisponibilidadRepositoryAdapter bloqueRepository;
    private InMemoryCitaRepositoryAdapter citaRepository;
    private Long cardio;
    private Long neuro;
    private Profesional profesional;

    @BeforeEach
    void setUp() {
        InMemoryEspecialidadRepositoryAdapter especialidadRepository = new InMemoryEspecialidadRepositoryAdapter();
        AdministrarEspecialidadesService especialidades = new AdministrarEspecialidadesService(especialidadRepository);
        cardio = especialidades.crear(new CrearCommand("CARDIO", "Cardiología", 30, false, true)).id();
        neuro = especialidades.crear(new CrearCommand("NEURO", "Neurología", 60, false, true)).id();

        profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        bloqueRepository = new InMemoryBloqueDisponibilidadRepositoryAdapter(new InMemoryDisponibilidadStore());
        citaRepository = new InMemoryCitaRepositoryAdapter();
        service = new ActualizarAsignacionesProfesionalService(profesionalRepository, especialidadRepository,
            new InMemoryLocationRepositoryAdapter(), bloqueRepository, citaRepository);

        profesional = profesionalRepository.guardar(Profesional.registrar(50L, "PROF-A", "MAT-A",
            Set.of(new AsignacionEspecialidad(cardio, true)), Set.of(1L, 2L)));
    }

    @Test
    void reemplazaEspecialidadesYSedesSinCambiarEstado() {
        var resultado = service.actualizar(profesional.getId(),
            List.of(new EspecialidadAsignadaCommand(cardio, false), new EspecialidadAsignadaCommand(neuro, true)),
            Set.of(1L));

        assertThat(resultado.sedeIds()).containsExactly(1L);
        Profesional guardado = profesionalRepository.buscarPorId(profesional.getId()).orElseThrow();
        assertThat(guardado.getEspecialidades()).extracting(AsignacionEspecialidad::especialidadId)
            .containsExactlyInAnyOrder(cardio, neuro);
        assertThat(guardado.isActivo()).isTrue();
        assertThat(guardado.getCodigoProfesional()).isEqualTo("PROF-A");
    }

    @Test
    void rechazaSinEspecialidadPrimaria() {
        assertThatThrownBy(() -> service.actualizar(profesional.getId(),
            List.of(new EspecialidadAsignadaCommand(cardio, false)), Set.of(1L)))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void rechazaSedeInexistente() {
        assertThatThrownBy(() -> service.actualizar(profesional.getId(),
            List.of(new EspecialidadAsignadaCommand(cardio, true)), Set.of(99L)))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void rechazaRetirarSedeConBloquesFuturos() {
        bloqueRepository.guardar(BloqueDisponibilidad.crear(profesional.getId(), 2L, LocalDate.now().plusDays(2),
            LocalTime.of(8, 0), LocalTime.of(12, 0), LocalDateTime.now()));

        assertThatThrownBy(() -> service.actualizar(profesional.getId(),
            List.of(new EspecialidadAsignadaCommand(cardio, true)), Set.of(1L)))
            .isInstanceOf(ValidacionNegocioException.class)
            .hasMessageContaining("sede 2");
    }

    @Test
    void rechazaRetirarEspecialidadConCitasFuturas() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(3);
        citaRepository.guardar(Cita.solicitarGeneral(100L, profesional.getId(), 1L, cardio, null, inicio,
            inicio.plusMinutes(30)));

        assertThatThrownBy(() -> service.actualizar(profesional.getId(),
            List.of(new EspecialidadAsignadaCommand(neuro, true)), Set.of(1L, 2L)))
            .isInstanceOf(ValidacionNegocioException.class)
            .hasMessageContaining("especialidad " + cardio);
    }

    @Test
    void profesionalInexistenteRecibe404() {
        assertThatThrownBy(() -> service.actualizar(999L, List.of(new EspecialidadAsignadaCommand(cardio, true)),
            Set.of(1L))).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
