package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.CerrarAtencionUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.FuenteCambioEstado;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CerrarAtencionService implements CerrarAtencionUseCase {

    private final CitaRepositoryPort citaRepository;
    private final ProfesionalRepositoryPort profesionalRepository;
    private final HistorialEstadoCitaPort historialPort;

    public CerrarAtencionService(CitaRepositoryPort citaRepository, ProfesionalRepositoryPort profesionalRepository,
                                  HistorialEstadoCitaPort historialPort) {
        this.citaRepository = citaRepository;
        this.profesionalRepository = profesionalRepository;
        this.historialPort = historialPort;
    }

    @Override
    @Transactional
    public Resultado completar(Long profesionalUsuarioId, Long citaId) {
        Cita cita = obtenerPropia(profesionalUsuarioId, citaId);
        LocalDateTime ahora = LocalDateTime.now();
        Cita guardada = citaRepository.guardar(cita.completar(ahora));
        historialPort.registrar(TransicionEstadoCita.nueva(citaId, EstadoCita.COMPLETED, profesionalUsuarioId,
            FuenteCambioEstado.USER, null, ahora));
        return new Resultado(guardada.getId(), guardada.getEstado().name());
    }

    @Override
    @Transactional
    public Resultado marcarNoShow(Long profesionalUsuarioId, Long citaId) {
        Cita cita = obtenerPropia(profesionalUsuarioId, citaId);
        LocalDateTime ahora = LocalDateTime.now();
        Cita guardada = citaRepository.guardar(cita.marcarNoShow(ahora));
        historialPort.registrar(TransicionEstadoCita.nueva(citaId, EstadoCita.NO_SHOW, profesionalUsuarioId,
            FuenteCambioEstado.USER, null, ahora));
        return new Resultado(guardada.getId(), guardada.getEstado().name());
    }

    private Cita obtenerPropia(Long profesionalUsuarioId, Long citaId) {
        Profesional profesional = profesionalRepository.buscarPorUsuarioId(profesionalUsuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado para el usuario actual"));
        Cita cita = citaRepository.buscarPorId(citaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));
        if (!cita.getProfesionalId().equals(profesional.getId())) {
            throw new RecursoNoEncontradoException("Cita no encontrada: " + citaId);
        }
        return cita;
    }
}
