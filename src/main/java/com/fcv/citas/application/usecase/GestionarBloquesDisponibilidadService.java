package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.GestionarBloquesDisponibilidadUseCase;
import com.fcv.citas.application.port.out.BloqueDisponibilidadRepositoryPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.BloqueDisponibilidad;
import com.fcv.citas.domain.model.Profesional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GestionarBloquesDisponibilidadService implements GestionarBloquesDisponibilidadUseCase {

    private final ProfesionalRepositoryPort profesionalRepository;
    private final BloqueDisponibilidadRepositoryPort bloqueRepository;

    public GestionarBloquesDisponibilidadService(ProfesionalRepositoryPort profesionalRepository,
                                                  BloqueDisponibilidadRepositoryPort bloqueRepository) {
        this.profesionalRepository = profesionalRepository;
        this.bloqueRepository = bloqueRepository;
    }

    @Override
    public Resultado crear(CrearCommand command) {
        Profesional profesional = resolverProfesionalActivo(command.usuarioId());
        requerirSedeHabilitada(profesional, command.sedeId());

        BloqueDisponibilidad bloque = BloqueDisponibilidad.crear(profesional.getId(), command.sedeId(),
            command.fecha(), command.horaInicio(), command.horaFin(), LocalDateTime.now());

        if (bloqueRepository.existeSolapamiento(profesional.getId(), command.fecha(), command.horaInicio(),
                command.horaFin(), null)) {
            throw new ValidacionNegocioException("El bloque se solapa con otro bloque existente del profesional");
        }

        return aResultado(bloqueRepository.guardar(bloque));
    }

    @Override
    public Resultado editar(EditarCommand command) {
        Profesional profesional = resolverProfesionalActivo(command.usuarioId());
        BloqueDisponibilidad actual = obtenerPropio(profesional, command.bloqueId());

        if (bloqueRepository.tieneSlotsComprometidos(actual.getId())) {
            throw new ValidacionNegocioException(
                "No se puede editar un bloque con al menos un slot ya reservado por una cita");
        }
        requerirSedeHabilitada(profesional, command.sedeId());

        BloqueDisponibilidad validado = BloqueDisponibilidad.crear(profesional.getId(), command.sedeId(),
            command.fecha(), command.horaInicio(), command.horaFin(), LocalDateTime.now());
        if (bloqueRepository.existeSolapamiento(profesional.getId(), command.fecha(), command.horaInicio(),
                command.horaFin(), actual.getId())) {
            throw new ValidacionNegocioException("El bloque se solapa con otro bloque existente del profesional");
        }

        BloqueDisponibilidad actualizado = BloqueDisponibilidad.reconstruir(actual.getId(), profesional.getId(),
            validado.getSedeId(), validado.getFecha(), validado.getHoraInicio(), validado.getHoraFin(), true);
        return aResultado(bloqueRepository.actualizarHorario(actualizado));
    }

    @Override
    public void eliminar(Long usuarioId, Long bloqueId) {
        Profesional profesional = resolverProfesionalActivo(usuarioId);
        BloqueDisponibilidad actual = obtenerPropio(profesional, bloqueId);
        if (bloqueRepository.tieneSlotsComprometidos(actual.getId())) {
            throw new ValidacionNegocioException(
                "No se puede eliminar un bloque con al menos un slot ya reservado por una cita");
        }
        bloqueRepository.eliminar(bloqueId);
    }

    @Override
    public List<Resultado> listarPropios(Long usuarioId) {
        Profesional profesional = profesionalRepository.buscarPorUsuarioId(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado para el usuario actual"));
        return bloqueRepository.listarPorProfesional(profesional.getId()).stream().map(this::aResultado).toList();
    }

    private Profesional resolverProfesionalActivo(Long usuarioId) {
        Profesional profesional = profesionalRepository.buscarPorUsuarioId(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado para el usuario actual"));
        if (!profesional.isActivo()) {
            throw new ValidacionNegocioException("Un profesional inactivo no puede publicar disponibilidad");
        }
        return profesional;
    }

    private void requerirSedeHabilitada(Profesional profesional, Long sedeId) {
        if (!profesional.tieneSedeHabilitada(sedeId)) {
            throw new ValidacionNegocioException("El profesional no está habilitado en la sede: " + sedeId);
        }
    }

    private BloqueDisponibilidad obtenerPropio(Profesional profesional, Long bloqueId) {
        BloqueDisponibilidad bloque = bloqueRepository.buscarPorId(bloqueId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Bloque de disponibilidad no encontrado: " + bloqueId));
        if (!bloque.getProfesionalId().equals(profesional.getId())) {
            throw new RecursoNoEncontradoException("Bloque de disponibilidad no encontrado: " + bloqueId);
        }
        return bloque;
    }

    private Resultado aResultado(BloqueDisponibilidad bloque) {
        return new Resultado(bloque.getId(), bloque.getProfesionalId(), bloque.getSedeId(), bloque.getFecha(),
            bloque.getHoraInicio(), bloque.getHoraFin(), bloque.isActivo());
    }
}
