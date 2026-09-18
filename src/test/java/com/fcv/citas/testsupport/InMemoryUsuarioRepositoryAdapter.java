package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.model.Usuario;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Doble de prueba de {@link UsuarioRepositoryPort}: reemplaza a
 * {@code UsuarioJpaAdapter} en el perfil "test" (sin MySQL disponible), vía
 * {@link InMemoryPersistenceTestConfig}. No usar en producción.
 */
public class InMemoryUsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final Map<String, Usuario> porId = new ConcurrentHashMap<>();
    private final Map<String, String> idPorEmail = new ConcurrentHashMap<>();
    private final Map<String, String> idPorDocumento = new ConcurrentHashMap<>();

    @Override
    public boolean existePorEmail(String email) {
        return email != null && idPorEmail.containsKey(email);
    }

    @Override
    public boolean existePorNumeroDocumento(String numeroDocumento) {
        return numeroDocumento != null && idPorDocumento.containsKey(numeroDocumento);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        porId.put(usuario.getId(), usuario);
        idPorEmail.put(usuario.getEmail(), usuario.getId());
        idPorDocumento.put(usuario.getNumeroDocumento(), usuario.getId());
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(idPorEmail.get(email)).map(porId::get);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(porId.get(id));
    }
}
