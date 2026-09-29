package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.CancelarCitaUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.FuenteCambioEstado;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelarCitaService implements CancelarCitaUseCase {

    private final CitaRepositoryPort citaRepository;
    private final SlotRepositoryPort slotRepository;
    private final HistorialEstadoCitaPort historialPort;

    public CancelarCitaService(CitaRepositoryPort citaRepository, SlotRepositoryPort slotRepository,
                                HistorialEstadoCitaPort historialPort) {
        this.citaRepository = citaRepository;
        this.slotRepository = slotRepository;
        this.historialPort = historialPort;
    }

    @Override
    @Transactional
    public Resultado cancelar(Long pacienteUsuarioId, Long citaId) {
        Cita cita = obtenerPropia(pacienteUsuarioId, citaId);
        LocalDateTime ahora = LocalDateTime.now();
        Cita cancelada = citaRepository.guardar(cita.cancelar(ahora));
        slotRepository.liberarSlotsDeCita(citaId);
        historialPort.registrar(TransicionEstadoCita.nueva(citaId, EstadoCita.CANCELLED, pacienteUsuarioId,
            FuenteCambioEstado.USER, null, ahora));
        return new Resultado(cancelada.getId(), cancelada.getEstado().name());
    }

    private Cita obtenerPropia(Long pacienteUsuarioId, Long citaId) {
        Cita cita = citaRepository.buscarPorId(citaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));
        if (!cita.getPacienteUsuarioId().equals(pacienteUsuarioId)) {
            throw new RecursoNoEncontradoException("Cita no encontrada: " + citaId);
        }
        return cita;
    }
}
