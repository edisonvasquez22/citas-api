package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.PasswordResetTokenRepositoryPort;
import com.fcv.citas.domain.model.TokenRecuperacion;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de prueba de {@link PasswordResetTokenRepositoryPort} (HU-003), sin MySQL. */
public class InMemoryPasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final Map<Long, TokenRecuperacion> porId = new ConcurrentHashMap<>();

    @Override
    public synchronized TokenRecuperacion guardar(TokenRecuperacion token) {
        TokenRecuperacion aGuardar = token;
        if (aGuardar.getId() == null) {
            Long nuevoId = secuenciaId.incrementAndGet();
            aGuardar = TokenRecuperacion.reconstruir(nuevoId, token.getUsuarioId(), token.getTokenHash(),
                token.getExpiraEn(), token.getUsadoEn());
        }
        porId.put(aGuardar.getId(), aGuardar);
        return aGuardar;
    }

    @Override
    public Optional<TokenRecuperacion> buscarPorHash(String tokenHash) {
        return porId.values().stream().filter(token -> token.getTokenHash().equals(tokenHash)).findFirst();
    }

    /** Solo para pruebas: el puerto real no expone un listado (el token nunca se filtra por HTTP, ver HU-003 CA-01). */
    public Collection<TokenRecuperacion> todos() {
        return porId.values();
    }
}
