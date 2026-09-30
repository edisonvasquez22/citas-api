package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.ConsultaCitasIntegracionPort;
import com.fcv.citas.domain.model.CitaNotificable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryConsultaCitasIntegracionAdapter implements ConsultaCitasIntegracionPort {

    private final List<CitaNotificable> citas = new ArrayList<>();

    public void agregar(CitaNotificable cita) {
        citas.add(cita);
    }

    @Override
    public List<CitaNotificable> listar(String estado, LocalDateTime desde, LocalDateTime hasta) {
        return citas.stream()
            .filter(c -> estado == null || c.estado().equals(estado))
            .filter(c -> !c.inicio().isBefore(desde) && c.inicio().isBefore(hasta))
            .toList();
    }

    @Override
    public Optional<CitaNotificable> buscarPorId(Long citaId) {
        return citas.stream().filter(c -> c.citaId().equals(citaId)).findFirst();
    }
}
