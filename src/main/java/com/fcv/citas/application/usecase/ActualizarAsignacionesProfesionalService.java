package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.ActualizarAsignacionesProfesionalUseCase;
import com.fcv.citas.application.port.in.RegistrarProfesionalUseCase.EspecialidadAsignadaCommand;
import com.fcv.citas.application.port.out.BloqueDisponibilidadRepositoryPort;
import com.fcv.citas.application.port.out.CitaRepositoryPort;
import com.fcv.citas.application.port.out.EspecialidadRepositoryPort;
import com.fcv.citas.application.port.out.LocationRepositoryPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Especialidad;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.Sede;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * No se permite quitar una sede con bloques de disponibilidad futuros, ni una especialidad con citas
 * futuras REQUESTED/APPROVED: dejaría agenda o citas vigentes fuera de las asignaciones del profesional.
 */
@Service
public class ActualizarAsignacionesProfesionalService implements ActualizarAsignacionesProfesionalUseCase {

    private final ProfesionalRepositoryPort profesionalRepository;
    private final EspecialidadRepositoryPort especialidadRepository;
    private final LocationRepositoryPort locationRepository;
    private final BloqueDisponibilidadRepositoryPort bloqueRepository;
    private final CitaRepositoryPort citaRepository;

    public ActualizarAsignacionesProfesionalService(ProfesionalRepositoryPort profesionalRepository,
                                                    EspecialidadRepositoryPort especialidadRepository,
                                                    LocationRepositoryPort locationRepository,
                                                    BloqueDisponibilidadRepositoryPort bloqueRepository,
                                                    CitaRepositoryPort citaRepository) {
        this.profesionalRepository = profesionalRepository;
        this.especialidadRepository = especialidadRepository;
        this.locationRepository = locationRepository;
        this.bloqueRepository = bloqueRepository;
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional
    public Resultado actualizar(Long profesionalId, List<EspecialidadAsignadaCommand> especialidades,
                                Set<Long> sedeIds) {
        Profesional profesional = profesionalRepository.buscarPorId(profesionalId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado: " + profesionalId));

        Set<AsignacionEspecialidad> nuevasEspecialidades = validarEspecialidades(especialidades);
        Set<Long> nuevasSedes = validarSedes(sedeIds);
        Profesional reasignado = profesional.reasignar(nuevasEspecialidades, nuevasSedes);

        verificarSedesRetiradas(profesional, nuevasSedes);
        verificarEspecialidadesRetiradas(profesional, nuevasEspecialidades);

        Profesional guardado = profesionalRepository.guardar(reasignado);
        return new Resultado(guardado.getId(),
            guardado.getEspecialidades().stream()
                .map(a -> new EspecialidadAsignadaCommand(a.especialidadId(), a.primaria()))
                .toList(),
            guardado.getSedeIds());
    }

    private Set<AsignacionEspecialidad> validarEspecialidades(List<EspecialidadAsignadaCommand> especialidades) {
        if (especialidades == null || especialidades.isEmpty()) {
            throw new ValidacionNegocioException("El profesional debe tener al menos una especialidad");
        }
        Set<AsignacionEspecialidad> resultado = new LinkedHashSet<>();
        for (EspecialidadAsignadaCommand asignacion : especialidades) {
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
        for (Long sedeId : sedeIds) {
            locationRepository.buscarPorId(sedeId)
                .filter(Sede::activa)
                .orElseThrow(() -> new ValidacionNegocioException("Sede no encontrada o inactiva: " + sedeId));
        }
        return new LinkedHashSet<>(sedeIds);
    }

    private void verificarSedesRetiradas(Profesional actual, Set<Long> nuevasSedes) {
        LocalDate hoy = LocalDate.now();
        bloqueRepository.listarPorProfesional(actual.getId()).stream()
            .filter(b -> b.isActivo() && !b.getFecha().isBefore(hoy) && !nuevasSedes.contains(b.getSedeId()))
            .findFirst()
            .ifPresent(b -> {
                throw new ValidacionNegocioException("No se puede retirar la sede " + b.getSedeId()
                    + ": el profesional tiene bloques de disponibilidad futuros en ella");
            });
    }

    private void verificarEspecialidadesRetiradas(Profesional actual, Set<AsignacionEspecialidad> nuevas) {
        Set<Long> nuevasIds = new LinkedHashSet<>();
        nuevas.forEach(a -> nuevasIds.add(a.especialidadId()));
        LocalDate hoy = LocalDate.now();
        Stream.of(EstadoCita.REQUESTED, EstadoCita.APPROVED)
            .flatMap(estado -> citaRepository.listarPorProfesional(actual.getId(), estado, null, hoy, null).stream())
            .filter(c -> !nuevasIds.contains(c.getEspecialidadId()))
            .findFirst()
            .ifPresent(c -> {
                throw new ValidacionNegocioException("No se puede retirar la especialidad " + c.getEspecialidadId()
                    + ": el profesional tiene citas futuras de esa especialidad");
            });
    }
}
