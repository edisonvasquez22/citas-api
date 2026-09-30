package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.GestionarPerfilUseCase;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.Usuario;
import com.fcv.citas.testsupport.InMemoryUsuarioRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-004: CA-01 a CA-03. */
class GestionarPerfilServiceTest {

    private InMemoryUsuarioRepositoryAdapter usuarioRepository;
    private GestionarPerfilService service;
    private String usuarioId;

    @BeforeEach
    void setUp() {
        usuarioRepository = new InMemoryUsuarioRepositoryAdapter();
        service = new GestionarPerfilService(usuarioRepository);
        Usuario guardado = usuarioRepository.guardar(Usuario.registrarNuevo("Ana", "Pérez", "CC", "123456789",
            "ana@example.com", "3001234567", "hash-x"));
        usuarioId = guardado.getId();
    }

    @Test
    void consultar_devuelveLosDatosPropios() {
        var resultado = service.consultar(usuarioId);

        assertThat(resultado.email()).isEqualTo("ana@example.com");
        assertThat(resultado.numeroDocumento()).isEqualTo("123456789");
    }

    @Test
    void consultar_usuarioInexistente_lanzaNoEncontrado() {
        assertThatThrownBy(() -> service.consultar("999")).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizar_conCamposValidos_quedaPersistido() {
        service.actualizar(usuarioId,
            new GestionarPerfilUseCase.ActualizarCommand("Ana María", "Pérez Gómez", "3009999999"));

        var consultado = service.consultar(usuarioId);
        assertThat(consultado.nombres()).isEqualTo("Ana María");
        assertThat(consultado.apellidos()).isEqualTo("Pérez Gómez");
        assertThat(consultado.telefono()).isEqualTo("3009999999");
    }

    @Test
    void actualizar_noModificaEmailNiDocumento() {
        service.actualizar(usuarioId,
            new GestionarPerfilUseCase.ActualizarCommand("Ana María", "Pérez Gómez", "3009999999"));

        var consultado = service.consultar(usuarioId);
        assertThat(consultado.email()).isEqualTo("ana@example.com");
        assertThat(consultado.numeroDocumento()).isEqualTo("123456789");
    }
}
