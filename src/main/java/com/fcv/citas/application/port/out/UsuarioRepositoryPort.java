package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.Usuario;
import java.util.Optional;

/**
 * Puerto de salida hacia persistencia. La implementación actual es en memoria
 * (ver infrastructure/adapter/out/persistence) hasta que exista el diseño 3FN
 * aprobado por el usuario; ver docs/wiki/llm-wiki/wiki/decisiones.md.
 */
public interface UsuarioRepositoryPort {

    boolean existePorEmail(String email);

    boolean existePorNumeroDocumento(String numeroDocumento);

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(String id);
}
