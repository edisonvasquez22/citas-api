package com.fcv.citas.infrastructure.config;

import com.fcv.citas.application.port.out.TokenProviderPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica cada request por access token (RF-02). Si el token falta o es
 * inválido simplemente no autentica: Spring Security decide 401/403 según la
 * regla de autorización del endpoint (ver SecurityConfig).
 *
 * Deliberadamente NO es un {@code @Component}: SecurityConfig lo instancia
 * directamente para que solo viva dentro de la cadena de Spring Security y no
 * quede también registrado como filtro de servlet genérico por Spring Boot.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProviderPort tokenProvider;

    public JwtAuthenticationFilter(TokenProviderPort tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                TokenProviderPort.TokenClaims claims = tokenProvider.validarAccessToken(token);
                List<SimpleGrantedAuthority> authorities = claims.roles().stream()
                    .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name()))
                    .toList();
                var authentication = new UsernamePasswordAuthenticationToken(claims.usuarioId(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
