package com.fcv.citas.infrastructure.config;

import com.fcv.citas.application.port.out.TokenProviderPort;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Sesión sin estado (JWT), sin AuthenticationManager/UserDetailsService: el
 * login se resuelve manualmente en IniciarSesionService contra
 * PasswordHasherPort. CORS restringido al origen configurado en
 * FRONTEND_ORIGIN (.env), ver RESTRICCIONES_TECNICAS.md.
 *
 * {@code exceptionHandling.authenticationEntryPoint} es necesario para que
 * una request sin token (o con uno inválido) responda 401, no 403: sin él,
 * Spring Security trata al anónimo como "autenticado sin permiso" en
 * cualquier endpoint con {@code hasRole(...)} y siempre devuelve 403 (ver
 * S3AuthorizationIntegrationTest y la convención 401 vs 403 de contratos.md).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenProviderPort tokenProvider;
    private final String allowedOrigin;

    public SecurityConfig(TokenProviderPort tokenProvider,
                           @Value("${app.cors.allowed-origin}") String allowedOrigin) {
        this.tokenProvider = tokenProvider;
        this.allowedOrigin = allowedOrigin;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/actuator/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // HU-009/HU-010/HU-011/HU-016: catálogos, profesionales y aprobación de citas son solo de ADMIN.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // HU-012: cada PROFESSIONAL gestiona únicamente sus propios bloques de disponibilidad.
                .requestMatchers("/api/professionals/me/**").hasRole("PROFESSIONAL")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
