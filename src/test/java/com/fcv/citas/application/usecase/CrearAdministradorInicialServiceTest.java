package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.CrearAdministradorInicialUseCase.Command;
import com.fcv.citas.application.port.in.CrearAdministradorInicialUseCase.Resultado;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.infrastructure.adapter.out.security.BCryptPasswordHasherAdapter;
import com.fcv.citas.testsupport.InMemoryUsuarioRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearAdministradorInicialServiceTest {

    private InMemoryUsuarioRepositoryAdapter usuarios;
    private CrearAdministradorInicialService service;

    @BeforeEach
    void setUp() {
        usuarios = new InMemoryUsuarioRepositoryAdapter();
        service = new CrearAdministradorInicialService(usuarios, new BCryptPasswordHasherAdapter());
    }

    private static Command comando(String email, String password) {
        return new Command(email, password, "Administrador", "Inicial", "ADMIN-0001", "0000000000");
    }

    @Test
    void creaElAdminCuandoNoExisteNinguno() {
        assertThat(service.crearSiNoExiste(comando("Root@Lab.Invalid", "ClaveAdminSegura1"))).isEqualTo(Resultado.CREADO);

        var admin = usuarios.buscarPorEmail("root@lab.invalid").orElseThrow();
        assertThat(admin.getRoles()).containsExactly(RolNombre.ADMIN);
        assertThat(admin.getPasswordHash()).isNotEqualTo("ClaveAdminSegura1");
    }

    @Test
    void noCreaOtroSiYaHayUnAdmin() {
        service.crearSiNoExiste(comando("root@lab.invalid", "ClaveAdminSegura1"));

        assertThat(service.crearSiNoExiste(comando("otro@lab.invalid", "ClaveAdminSegura2")))
            .isEqualTo(Resultado.YA_EXISTE_ADMIN);
        assertThat(usuarios.buscarPorEmail("otro@lab.invalid")).isEmpty();
    }

    @Test
    void noConvierteEnAdminUnaCuentaExistente() {
        usuarios.guardar(com.fcv.citas.domain.model.Usuario.registrarNuevo("Ana", "Paz", "CC", "123", "ana@lab.invalid",
            "300", "hash"));

        assertThat(service.crearSiNoExiste(comando("ana@lab.invalid", "ClaveAdminSegura1")))
            .isEqualTo(Resultado.EMAIL_EN_USO);
        assertThat(usuarios.buscarPorEmail("ana@lab.invalid").orElseThrow().getRoles()).containsExactly(RolNombre.USER);
    }

    @Test
    void rechazaContrasenaCorta() {
        assertThatThrownBy(() -> service.crearSiNoExiste(comando("root@lab.invalid", "corta")))
            .isInstanceOf(ValidacionNegocioException.class);
    }
}
