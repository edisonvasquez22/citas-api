package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.ListarProfesionalesUseCase;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.Usuario;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ListarProfesionalesService implements ListarProfesionalesUseCase {

    private final ProfesionalRepositoryPort profesionalRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public ListarProfesionalesService(ProfesionalRepositoryPort profesionalRepository,
                                       UsuarioRepositoryPort usuarioRepository) {
        this.profesionalRepository = profesionalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public java.util.List<Resultado> listarActivos() {
        return profesionalRepository.listarActivos().stream().map(this::aResultado).toList();
    }

    private Resultado aResultado(Profesional profesional) {
        String nombreCompleto = usuarioRepository.buscarPorId(String.valueOf(profesional.getUsuarioId()))
            .map(u -> u.getNombres() + " " + u.getApellidos())
            .orElse("Profesional " + profesional.getCodigoProfesional());
        Set<Long> especialidadIds = profesional.getEspecialidades().stream()
            .map(AsignacionEspecialidad::especialidadId)
            .collect(Collectors.toSet());
        return new Resultado(profesional.getId(), nombreCompleto, especialidadIds, profesional.getSedeIds());
    }
}
