package com.fcv.citas.infrastructure.adapter.in.web;

import com.fcv.citas.application.port.in.ConsultarHistorialCitaUseCase;
import com.fcv.citas.domain.model.RolNombre;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** HU-023 / RF-19: solo lectura; no existe endpoint de edición ni borrado (RN-12). */
@RestController
public class AppointmentHistoryController {

    private final ConsultarHistorialCitaUseCase useCase;

    public AppointmentHistoryController(ConsultarHistorialCitaUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/api/appointments/{id}/history")
    public List<HistorialResponse> historial(Authentication authentication, @PathVariable Long id) {
        return useCase.consultar(Long.valueOf(authentication.getName()), roles(authentication), id).stream()
            .map(t -> new HistorialResponse(t.estado().name(), t.fuente().name(), t.actorUsuarioId(), t.motivo(),
                t.momento()))
            .toList();
    }

    private static Set<RolNombre> roles(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring("ROLE_".length()))
            .filter(a -> Set.of("USER", "PROFESSIONAL", "ADMIN").contains(a))
            .map(RolNombre::valueOf)
            .collect(Collectors.toSet());
    }

    public record HistorialResponse(String estado, String fuente, Long actorUsuarioId, String motivo,
                                    LocalDateTime momento) {}
}
