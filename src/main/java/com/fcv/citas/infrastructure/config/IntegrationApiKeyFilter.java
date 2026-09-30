package com.fcv.citas.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica a n8n en /api/integration/** con una API key de mínimo privilegio (solo ROLE_INTEGRATION,
 * que únicamente habilita esas rutas de lectura). Si la clave no está configurada, nadie entra.
 */
public class IntegrationApiKeyFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Integration-Key";
    private static final String PREFIJO_RUTA = "/api/integration/";

    private final byte[] apiKey;

    public IntegrationApiKeyFilter(String apiKey) {
        this.apiKey = apiKey == null ? new byte[0] : apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PREFIJO_RUTA);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String recibida = request.getHeader(HEADER);
        if (apiKey.length > 0 && recibida != null
            && MessageDigest.isEqual(apiKey, recibida.getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "n8n-integration", null, List.of(new SimpleGrantedAuthority("ROLE_INTEGRATION"))));
        }
        chain.doFilter(request, response);
    }
}
