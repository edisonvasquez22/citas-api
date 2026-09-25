package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.Usuario;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryUsuarioRepositoryAdapter;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Soporte de HU-013/014/015: directorio público de profesionales activos. */
class ListarProfesionalesServiceTest {

    @Test
    void listarActivos_devuelveNombreCompletoResueltoDesdeElUsuario() {
        InMemoryUsuarioRepositoryAdapter usuarioRepository = new InMemoryUsuarioRepositoryAdapter();
        InMemoryProfesionalRepositoryAdapter profesionalRepository = new InMemoryProfesionalRepositoryAdapter();

        Usuario usuario = usuarioRepository.guardar(Usuario.registrarProfesional("Valentina", "Rincón", "CC",
            "1000000005", "valentina@example.com", "3000000000", "hash"));
        profesionalRepository.guardar(Profesional.registrar(Long.valueOf(usuario.getId()), "PROF-001", "MAT-001",
            Set.of(new AsignacionEspecialidad(10L, true)), Set.of(1L)));

        var service = new ListarProfesionalesService(profesionalRepository, usuarioRepository);
        var resultado = service.listarActivos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombreCompleto()).isEqualTo("Valentina Rincón");
        assertThat(resultado.get(0).especialidadIds()).containsExactly(10L);
        assertThat(resultado.get(0).sedeIds()).containsExactly(1L);
    }

    @Test
    void listarActivos_sinProfesionales_devuelveListaVacia() {
        var service = new ListarProfesionalesService(new InMemoryProfesionalRepositoryAdapter(),
            new InMemoryUsuarioRepositoryAdapter());

        assertThat(service.listarActivos()).isEmpty();
    }
}
