package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.RefreshTokenStorePort;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Doble de prueba de {@link RefreshTokenStorePort}: reemplaza a
 * {@code RefreshTokenJpaAdapter} en el perfil "test". No usar en producción.
 */
public class InMemoryRefreshTokenStoreAdapter implements RefreshTokenStorePort {

    private final Map<String, Instant> vigentes = new ConcurrentHashMap<>();

    @Override
    public void registrar(String jti, String usuarioId, Instant expiraEn) {
        vigentes.put(jti, expiraEn);
    }

    @Override
    public boolean estaVigente(String jti) {
        Instant expiraEn = vigentes.get(jti);
        return expiraEn != null && expiraEn.isAfter(Instant.now());
    }

    @Override
    public void revocar(String jti) {
        vigentes.remove(jti);
    }
}
