package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.CancelarCitaUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.application.port.out.NotificadorCambioEstadoPort;
import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.EventoCambioEstado;
import com.fcv.citas.domain.model.TipoEventoCita;
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
    private final NotificadorCambioEstadoPort notificador;

    public CancelarCitaService(CitaRepositoryPort citaRepository, SlotRepositoryPort slotRepository,
                                HistorialEstadoCitaPort historialPort, NotificadorCambioEstadoPort notificador) {
        this.citaRepository = citaRepository;
        this.slotRepository = slotRepository;
        this.historialPort = historialPort;
        this.notificador = notificador;
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
        notificador.notificar(EventoCambioEstado.deCita(TipoEventoCita.CITA_CANCELADA, citaId, null));
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
