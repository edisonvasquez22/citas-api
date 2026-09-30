package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.CrearAdministradorInicialUseCase;
import com.fcv.citas.application.port.out.PasswordHasherPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.domain.model.Usuario;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearAdministradorInicialService implements CrearAdministradorInicialUseCase {

    static final int LONGITUD_MINIMA_PASSWORD = 12;

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordHasherPort passwordHasher;

    public CrearAdministradorInicialService(UsuarioRepositoryPort usuarioRepository,
                                            PasswordHasherPort passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public Resultado crearSiNoExiste(Command command) {
        if (usuarioRepository.existeConRol(RolNombre.ADMIN)) {
            return Resultado.YA_EXISTE_ADMIN;
        }
        if (command.password() == null || command.password().length() < LONGITUD_MINIMA_PASSWORD) {
            throw new ValidacionNegocioException(
                "La contraseña del ADMIN inicial debe tener al menos " + LONGITUD_MINIMA_PASSWORD + " caracteres");
        }
        String email = command.email() == null ? null : command.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existePorEmail(email)) {
            return Resultado.EMAIL_EN_USO;
        }
        usuarioRepository.guardar(Usuario.registrarAdministrador(command.nombres(), command.apellidos(), "CC",
            command.numeroDocumento(), email, command.telefono(), passwordHasher.hash(command.password())));
        return Resultado.CREADO;
    }
}
