package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.AdministrarEspecialidadesUseCase;
import com.fcv.citas.application.port.out.EspecialidadRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.Especialidad;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdministrarEspecialidadesService implements AdministrarEspecialidadesUseCase {

    private final EspecialidadRepositoryPort especialidadRepository;

    public AdministrarEspecialidadesService(EspecialidadRepositoryPort especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    public Resultado crear(CrearCommand command) {
        if (especialidadRepository.existePorCodigo(command.codigo())) {
            throw new ValidacionNegocioException("Ya existe una especialidad con el código: " + command.codigo());
        }
        Especialidad especialidad = Especialidad.crear(command.codigo(), command.nombre(), command.duracionMinutos(),
            command.general(), command.requiereAprobacionAdmin());
        return aResultado(especialidadRepository.guardar(especialidad));
    }

    @Override
    public Resultado editar(Long id, EditarCommand command) {
        Especialidad actual = obtener(id);
        Especialidad editada = actual.editar(command.nombre(), command.duracionMinutos(), command.general(),
            command.requiereAprobacionAdmin());
        return aResultado(especialidadRepository.guardar(editada));
    }

    @Override
    public Resultado cambiarEstado(Long id, boolean activa) {
        Especialidad actual = obtener(id);
        return aResultado(especialidadRepository.guardar(actual.cambiarEstado(activa)));
    }

    @Override
    public List<Resultado> listar() {
        return especialidadRepository.listar().stream().map(this::aResultado).toList();
    }

    private Especialidad obtener(Long id) {
        return especialidadRepository.buscarPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada: " + id));
    }

    private Resultado aResultado(Especialidad especialidad) {
        return new Resultado(especialidad.getId(), especialidad.getCodigo(), especialidad.getNombre(),
            especialidad.getDuracionMinutos(), especialidad.isGeneral(), especialidad.isRequiereAprobacionAdmin(),
            especialidad.isActiva());
    }
}
