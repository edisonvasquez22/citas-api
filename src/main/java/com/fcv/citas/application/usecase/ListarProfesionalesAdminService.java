package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.ListarProfesionalesAdminUseCase;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.Usuario;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ListarProfesionalesAdminService implements ListarProfesionalesAdminUseCase {

    private final ProfesionalRepositoryPort profesionalRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public ListarProfesionalesAdminService(ProfesionalRepositoryPort profesionalRepository,
                                            UsuarioRepositoryPort usuarioRepository) {
        this.profesionalRepository = profesionalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Resultado> listarTodos() {
        return profesionalRepository.listarTodos().stream().map(this::aResultado).toList();
    }

    private Resultado aResultado(Profesional profesional) {
        Usuario usuario = usuarioRepository.buscarPorId(String.valueOf(profesional.getUsuarioId())).orElse(null);
        Set<EspecialidadAsignadaResultado> especialidades = profesional.getEspecialidades().stream()
            .map(a -> new EspecialidadAsignadaResultado(a.especialidadId(), a.primaria()))
            .collect(Collectors.toSet());

        return new Resultado(profesional.getId(), profesional.getUsuarioId(),
            usuario != null ? usuario.getNombres() : null, usuario != null ? usuario.getApellidos() : null,
            usuario != null ? usuario.getTipoDocumento() : null, usuario != null ? usuario.getNumeroDocumento() : null,
            usuario != null ? usuario.getEmail() : null, usuario != null ? usuario.getTelefono() : null,
            profesional.getCodigoProfesional(), profesional.getMatricula(), profesional.isActivo(), especialidades,
            profesional.getSedeIds());
    }
}
