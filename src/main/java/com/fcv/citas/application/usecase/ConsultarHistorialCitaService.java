package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.ConsultarHistorialCitaUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Fuera de ownership responde 404 (no 403), igual que el resto de recursos propios, para no revelar existencia. */
@Service
public class ConsultarHistorialCitaService implements ConsultarHistorialCitaUseCase {

    private final CitaRepositoryPort citaRepository;
    private final ProfesionalRepositoryPort profesionalRepository;
    private final HistorialEstadoCitaPort historialPort;

    public ConsultarHistorialCitaService(CitaRepositoryPort citaRepository,
                                         ProfesionalRepositoryPort profesionalRepository,
                                         HistorialEstadoCitaPort historialPort) {
        this.citaRepository = citaRepository;
        this.profesionalRepository = profesionalRepository;
        this.historialPort = historialPort;
    }

    @Override
    public List<TransicionEstadoCita> consultar(Long usuarioId, Set<RolNombre> roles, Long citaId) {
        Cita cita = citaRepository.buscarPorId(citaId)
            .filter(c -> puedeVer(usuarioId, roles, c))
            .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));
        return historialPort.listarPorCita(cita.getId());
    }

    private boolean puedeVer(Long usuarioId, Set<RolNombre> roles, Cita cita) {
        if (roles.contains(RolNombre.ADMIN) || cita.getPacienteUsuarioId().equals(usuarioId)) {
            return true;
        }
        return roles.contains(RolNombre.PROFESSIONAL)
            && profesionalRepository.buscarPorUsuarioId(usuarioId)
                .map(Profesional::getId)
                .filter(id -> id.equals(cita.getProfesionalId()))
                .isPresent();
    }
}
