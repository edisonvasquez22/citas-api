package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase;
import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase.Command;
import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase.EspecialidadAsignadaCommand;
import com.fcv.citas.infrastructure.adapter.out.security.BCryptPasswordHasherAdapter;
import com.fcv.citas.testsupport.InMemoryEspecialidadRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryLocationRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryUsuarioRepositoryAdapter;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-011: la bandeja administrativa debe listar profesionales activos e inactivos, con datos de contacto reales. */
class ListarProfesionalesAdminServiceTest {

    private InMemoryProfesionalRepositoryAdapter profesionalRepository;
    private CambiarEstadoProfesionalService cambiarEstadoService;
    private ListarProfesionalesAdminService listarService;
    private Long profesionalId;

    @BeforeEach
    void setUp() {
        InMemoryEspecialidadRepositoryAdapter especialidadRepository = new InMemoryEspecialidadRepositoryAdapter();
        AdministrarEspecialidadesService especialidadesService = new AdministrarEspecialidadesService(especialidadRepository);
        InMemoryUsuarioRepositoryAdapter usuarioRepository = new InMemoryUsuarioRepositoryAdapter();
        profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        RegistrarProfesionalService registrarService = new RegistrarProfesionalService(usuarioRepository,
            new BCryptPasswordHasherAdapter(), especialidadRepository, new InMemoryLocationRepositoryAdapter(),
            profesionalRepository);
        cambiarEstadoService = new CambiarEstadoProfesionalService(profesionalRepository);
        listarService = new ListarProfesionalesAdminService(profesionalRepository, usuarioRepository);

        var especialidad = especialidadesService.crear(
            new AdministrarEspecialidadesUseCase.CrearCommand("CARDIO", "Cardiología", 30, false, true));
        var registrado = registrarService.registrar(new Command("Ana", "Gómez", "CC", "1000000002",
            "ana.gomez@example.com", "3000000000", "clave-segura-1", "PROF-001", "MAT-001",
            List.of(new EspecialidadAsignadaCommand(especialidad.id(), true)), Set.of(1L)));
        profesionalId = registrado.profesionalId();
    }

    @Test
    void listarTodos_incluyeActivosConDatosDeContactoReales() {
        var resultados = listarService.listarTodos();

        assertThat(resultados).hasSize(1);
        var r = resultados.get(0);
        assertThat(r.profesionalId()).isEqualTo(profesionalId);
        assertThat(r.nombres()).isEqualTo("Ana");
        assertThat(r.apellidos()).isEqualTo("Gómez");
        assertThat(r.email()).isEqualTo("ana.gomez@example.com");
        assertThat(r.activo()).isTrue();
    }

    @Test
    void listarTodos_incluyeInactivos() {
        cambiarEstadoService.cambiarEstado(profesionalId, false);

        var resultados = listarService.listarTodos();

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).activo()).isFalse();
    }
}
