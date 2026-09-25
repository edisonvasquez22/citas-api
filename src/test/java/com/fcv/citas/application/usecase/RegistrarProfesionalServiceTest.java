package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase;
import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase.Command;
import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase.EspecialidadAsignadaCommand;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.infrastructure.adapter.out.security.BCryptPasswordHasherAdapter;
import com.fcv.citas.testsupport.InMemoryEspecialidadRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryLocationRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryUsuarioRepositoryAdapter;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-010: CA-01 a CA-03 (+ sede inexistente, tarea T-03). */
class RegistrarProfesionalServiceTest {

    private RegistrarProfesionalService service;
    private AdministrarEspecialidadesService especialidadesService;

    @BeforeEach
    void setUp() {
        InMemoryEspecialidadRepositoryAdapter especialidadRepository = new InMemoryEspecialidadRepositoryAdapter();
        especialidadesService = new AdministrarEspecialidadesService(especialidadRepository);
        service = new RegistrarProfesionalService(new InMemoryUsuarioRepositoryAdapter(),
            new BCryptPasswordHasherAdapter(), especialidadRepository, new InMemoryLocationRepositoryAdapter(),
            new InMemoryProfesionalRepositoryAdapter());
    }

    private Command comandoValido(long especialidadId, Set<Long> sedeIds) {
        return new Command("Ana", "Gómez", "CC", "1000000002", "ana.gomez@example.com", "3000000000",
            "clave-segura-1", "PROF-001", "MAT-001", List.of(new EspecialidadAsignadaCommand(especialidadId, true)),
            sedeIds);
    }

    @Test
    void registrar_conDatosValidos_creaElProfesionalHabilitado() {
        var especialidad = especialidadesService.crear(
            new com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand("CARDIO",
                "Cardiología", 30, false, true));

        var resultado = service.registrar(comandoValido(especialidad.id(), Set.of(1L)));

        assertThat(resultado.profesionalId()).isNotNull();
        assertThat(resultado.activo()).isTrue();
    }

    @Test
    void registrar_conDosEspecialidadesPrimarias_seRechaza() {
        var e1 = especialidadesService.crear(
            new com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand("CARDIO",
                "Cardiología", 30, false, true));
        var e2 = especialidadesService.crear(
            new com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand("NEURO",
                "Neurología", 60, false, true));
        Command command = new Command("Ana", "Gómez", "CC", "1000000002", "ana.gomez@example.com", "3000000000",
            "clave-segura-1", "PROF-001", "MAT-001",
            List.of(new EspecialidadAsignadaCommand(e1.id(), true), new EspecialidadAsignadaCommand(e2.id(), true)),
            Set.of(1L));

        assertThatThrownBy(() -> service.registrar(command)).isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void registrar_conEspecialidadInactiva_seRechaza() {
        var especialidad = especialidadesService.crear(
            new com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand("CARDIO",
                "Cardiología", 30, false, true));
        especialidadesService.cambiarEstado(especialidad.id(), false);

        assertThatThrownBy(() -> service.registrar(comandoValido(especialidad.id(), Set.of(1L))))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void registrar_conSedeInexistente_seRechaza() {
        var especialidad = especialidadesService.crear(
            new com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase.CrearCommand("CARDIO",
                "Cardiología", 30, false, true));

        assertThatThrownBy(() -> service.registrar(comandoValido(especialidad.id(), Set.of(999L))))
            .isInstanceOf(ValidacionNegocioException.class);
    }
}
