package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.ConsultarAgendaPropiaUseCase;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.Profesional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsultarAgendaPropiaService implements ConsultarAgendaPropiaUseCase {

    private final CitaRepositoryPort citaRepository;
    private final ProfesionalRepositoryPort profesionalRepository;

    public ConsultarAgendaPropiaService(CitaRepositoryPort citaRepository,
                                         ProfesionalRepositoryPort profesionalRepository) {
        this.citaRepository = citaRepository;
        this.profesionalRepository = profesionalRepository;
    }

    @Override
    public List<Resultado> listar(Long profesionalUsuarioId, Long sedeId, LocalDate desde, LocalDate hasta) {
        Profesional profesional = profesionalRepository.buscarPorUsuarioId(profesionalUsuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado para el usuario actual"));
        return citaRepository.listarPorProfesional(profesional.getId(), EstadoCita.APPROVED, sedeId, desde, hasta)
            .stream()
            .map(c -> new Resultado(c.getId(), c.getPacienteUsuarioId(), c.getSedeId(), c.getEspecialidadId(),
                c.getInicio(), c.getFin()))
            .toList();
    }
}
