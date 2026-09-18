package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.RegistrarUsuarioUseCase;
import com.fcv.citas.application.port.out.PasswordHasherPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.DocumentoYaRegistradoException;
import com.fcv.citas.domain.exception.EmailYaRegistradoException;
import com.fcv.citas.domain.model.Usuario;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordHasherPort passwordHasher;

    public RegistrarUsuarioService(UsuarioRepositoryPort usuarioRepository, PasswordHasherPort passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Resultado registrar(Command command) {
        String email = command.email() == null ? null : command.email().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existePorEmail(email)) {
            throw new EmailYaRegistradoException(email);
        }
        if (usuarioRepository.existePorNumeroDocumento(command.numeroDocumento())) {
            throw new DocumentoYaRegistradoException(command.numeroDocumento());
        }

        String passwordHash = passwordHasher.hash(command.password());
        Usuario usuario = Usuario.registrarNuevo(
            command.nombres(),
            command.apellidos(),
            command.tipoDocumento(),
            command.numeroDocumento(),
            email,
            command.telefono(),
            passwordHash
        );

        Usuario guardado = usuarioRepository.guardar(usuario);
        return new Resultado(guardado.getId(), guardado.getEmail());
    }
}
