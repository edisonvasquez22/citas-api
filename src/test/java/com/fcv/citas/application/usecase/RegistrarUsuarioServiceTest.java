package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fcv.citas.application.port.in.RegistrarUsuarioUseCase.Command;
import com.fcv.citas.application.port.in.RegistrarUsuarioUseCase.Resultado;
import com.fcv.citas.application.port.out.PasswordHasherPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.DocumentoYaRegistradoException;
import com.fcv.citas.domain.exception.EmailYaRegistradoException;
import com.fcv.citas.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordHasherPort passwordHasher;

    private RegistrarUsuarioService service;

    private static final Command COMMAND_VALIDO = new Command(
        "Ana", "Pérez", "CC", "1000000001", "ana@example.com", "3000000000", "clave-segura-1"
    );

    @BeforeEach
    void setUp() {
        service = new RegistrarUsuarioService(usuarioRepository, passwordHasher);
    }

    @Test
    void registrar_conDatosValidos_creaUsuarioConPasswordHasheado() {
        when(usuarioRepository.existePorEmail("ana@example.com")).thenReturn(false);
        when(usuarioRepository.existePorNumeroDocumento("1000000001")).thenReturn(false);
        when(passwordHasher.hash("clave-segura-1")).thenReturn("hash-seguro");
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Resultado resultado = service.registrar(COMMAND_VALIDO);

        assertThat(resultado.email()).isEqualTo("ana@example.com");
        assertThat(resultado.usuarioId()).isNotBlank();
    }

    @Test
    void registrar_conEmailDuplicado_lanzaExcepcionDeConflicto() {
        when(usuarioRepository.existePorEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(COMMAND_VALIDO))
            .isInstanceOf(EmailYaRegistradoException.class);
    }

    @Test
    void registrar_conDocumentoDuplicado_lanzaExcepcionDeConflicto() {
        when(usuarioRepository.existePorEmail("ana@example.com")).thenReturn(false);
        when(usuarioRepository.existePorNumeroDocumento("1000000001")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(COMMAND_VALIDO))
            .isInstanceOf(DocumentoYaRegistradoException.class);
    }
}
