package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.model.Usuario;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Doble de prueba de {@link UsuarioRepositoryPort}: reemplaza a
 * {@code UsuarioJpaAdapter} en el perfil "test" (sin MySQL disponible), vía
 * {@link InMemoryPersistenceTestConfig}. No usar en producción.
 *
 * Simula la misma semántica que la base real: el id (autoincremental en
 * MySQL) lo asigna {@code guardar}, no el dominio.
 */
public class InMemoryUsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
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
        Usuario aGuardar = usuario;
        if (aGuardar.getId() == null) {
            String nuevoId = String.valueOf(secuenciaId.incrementAndGet());
            aGuardar = Usuario.reconstruir(nuevoId, usuario.getNombres(), usuario.getApellidos(),
                usuario.getTipoDocumento(), usuario.getNumeroDocumento(), usuario.getEmail(),
                usuario.getTelefono(), usuario.getPasswordHash(), usuario.getRoles(), usuario.isActivo());
        }
        porId.put(aGuardar.getId(), aGuardar);
        idPorEmail.put(aGuardar.getEmail(), aGuardar.getId());
        idPorDocumento.put(aGuardar.getNumeroDocumento(), aGuardar.getId());
        return aGuardar;
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
