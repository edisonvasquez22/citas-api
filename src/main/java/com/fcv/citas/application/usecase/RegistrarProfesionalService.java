package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase;
import com.fcv.citas.application.port.out.EspecialidadRepositoryPort;
import com.fcv.citas.application.port.out.LocationRepositoryPort;
import com.fcv.citas.application.port.out.PasswordHasherPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.application.port.out.UsuarioRepositoryPort;
import com.fcv.citas.domain.exception.DocumentoYaRegistradoException;
import com.fcv.citas.domain.exception.EmailYaRegistradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Especialidad;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.Usuario;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RegistrarProfesionalService implements RegistrarProfesionalUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordHasherPort passwordHasher;
    private final EspecialidadRepositoryPort especialidadRepository;
    private final LocationRepositoryPort locationRepository;
    private final ProfesionalRepositoryPort profesionalRepository;

    public RegistrarProfesionalService(UsuarioRepositoryPort usuarioRepository, PasswordHasherPort passwordHasher,
                                        EspecialidadRepositoryPort especialidadRepository,
                                        LocationRepositoryPort locationRepository,
                                        ProfesionalRepositoryPort profesionalRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.especialidadRepository = especialidadRepository;
        this.locationRepository = locationRepository;
        this.profesionalRepository = profesionalRepository;
    }

    @Override
    public Resultado registrar(Command command) {
        String email = command.email() == null ? null : command.email().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existePorEmail(email)) {
            throw new EmailYaRegistradoException(email);
        }
        if (usuarioRepository.existePorNumeroDocumento(command.numeroDocumento())) {
            throw new DocumentoYaRegistradoException(command.numeroDocumento());
        }
        if (command.codigoProfesional() != null && profesionalRepository.existeCodigoProfesional(command.codigoProfesional())) {
            throw new ValidacionNegocioException(
                "Ya existe un profesional con el código: " + command.codigoProfesional());
        }
        if (command.matricula() != null && profesionalRepository.existeMatricula(command.matricula())) {
            throw new ValidacionNegocioException("Ya existe un profesional con la matrícula: " + command.matricula());
        }

        Set<AsignacionEspecialidad> especialidades = validarEspecialidades(command);
        Set<Long> sedeIds = validarSedes(command.sedeIds());

        String passwordHash = passwordHasher.hash(command.password());
        Usuario usuario = Usuario.registrarProfesional(command.nombres(), command.apellidos(),
            command.tipoDocumento(), command.numeroDocumento(), email, command.telefono(), passwordHash);
        Usuario usuarioGuardado = usuarioRepository.guardar(usuario);

        Profesional profesional = Profesional.registrar(Long.valueOf(usuarioGuardado.getId()),
            command.codigoProfesional(), command.matricula(), especialidades, sedeIds);
        Profesional guardado = profesionalRepository.guardar(profesional);

        return new Resultado(guardado.getId(), guardado.getUsuarioId(), guardado.getCodigoProfesional(),
            guardado.isActivo());
    }

    private Set<AsignacionEspecialidad> validarEspecialidades(Command command) {
        if (command.especialidades() == null || command.especialidades().isEmpty()) {
            throw new ValidacionNegocioException("El profesional debe tener al menos una especialidad");
        }
        Set<AsignacionEspecialidad> resultado = new LinkedHashSet<>();
        for (EspecialidadAsignadaCommand asignacion : command.especialidades()) {
            Especialidad especialidad = especialidadRepository.buscarPorId(asignacion.especialidadId())
                .orElseThrow(() -> new ValidacionNegocioException(
                    "Especialidad no encontrada: " + asignacion.especialidadId()));
            if (!especialidad.isActiva()) {
                throw new ValidacionNegocioException(
                    "No se puede asignar una especialidad inactiva: " + especialidad.getCodigo());
            }
            resultado.add(new AsignacionEspecialidad(asignacion.especialidadId(), asignacion.primaria()));
        }
        return resultado;
    }

    private Set<Long> validarSedes(Set<Long> sedeIds) {
        if (sedeIds == null || sedeIds.isEmpty()) {
            throw new ValidacionNegocioException("El profesional debe tener al menos una sede");
        }
        return sedeIds.stream()
            .peek(sedeId -> locationRepository.buscarPorId(sedeId)
                .filter(com.fcv.citas.domain.model.Sede::activa)
                .orElseThrow(() -> new ValidacionNegocioException("Sede no encontrada o inactiva: " + sedeId)))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
