package com.fcv.citas.application.port.out;

import com.fcv.citas.domain.model.CitaNotificable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultaCitasIntegracionPort {

    /** {@code estado} nulo = todos los estados. Rango semiabierto [desde, hasta). */
    List<CitaNotificable> listar(String estado, LocalDateTime desde, LocalDateTime hasta);

    Optional<CitaNotificable> buscarPorId(Long citaId);
}
