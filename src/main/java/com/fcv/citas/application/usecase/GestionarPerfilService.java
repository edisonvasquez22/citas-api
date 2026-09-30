package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.GestionarPerfilUseCase;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.Usuario;
import org.springframework.stereotype.Service;

/**
 * HU-004: el propio usuario consulta/actualiza su perfil. El ownership queda
 * garantizado por diseño: {@code usuarioId} siempre viene del JWT
 * (Authentication#getName() en el controller), nunca de un parámetro que el
 * cliente pueda manipular para ver/editar a otro usuario.
 */
@Service
public class GestionarPerfilService implements GestionarPerfilUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public GestionarPerfilService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Resultado consultar(String usuarioId) {
        return aResultado(obtener(usuarioId));
    }

    @Override
    public Resultado actualizar(String usuarioId, ActualizarCommand command) {
        Usuario actual = obtener(usuarioId);
        Usuario actualizado = actual.actualizarPerfil(command.nombres(), command.apellidos(), command.telefono());
        return aResultado(usuarioRepository.guardar(actualizado));
    }

    private Usuario obtener(String usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));
    }

    private Resultado aResultado(Usuario usuario) {
        return new Resultado(usuario.getId(), usuario.getNombres(), usuario.getApellidos(),
            usuario.getTipoDocumento(), usuario.getNumeroDocumento(), usuario.getEmail(), usuario.getTelefono());
    }
}
