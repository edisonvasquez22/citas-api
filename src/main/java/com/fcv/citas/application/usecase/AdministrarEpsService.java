package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.AdministrarEpsUseCase;
import com.fcv.citas.application.port.out.EpsRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.Eps;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdministrarEpsService implements AdministrarEpsUseCase {

    private final EpsRepositoryPort epsRepository;

    public AdministrarEpsService(EpsRepositoryPort epsRepository) {
        this.epsRepository = epsRepository;
    }

    @Override
    public Resultado crear(CrearCommand command) {
        if (epsRepository.existePorCodigo(command.codigo())) {
            throw new ValidacionNegocioException("Ya existe una EPS con el código: " + command.codigo());
        }
        Eps eps = Eps.crear(command.codigo(), command.nombre());
        return aResultado(epsRepository.guardar(eps));
    }

    @Override
    public Resultado editar(Long id, EditarCommand command) {
        Eps actual = obtener(id);
        return aResultado(epsRepository.guardar(actual.editar(command.nombre())));
    }

    @Override
    public Resultado cambiarEstado(Long id, boolean activa) {
        Eps actual = obtener(id);
        return aResultado(epsRepository.guardar(actual.cambiarEstado(activa)));
    }

    @Override
    public List<Resultado> listar() {
        return epsRepository.listar().stream().map(this::aResultado).toList();
    }

    private Eps obtener(Long id) {
        return epsRepository.buscarPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("EPS no encontrada: " + id));
    }

    private Resultado aResultado(Eps eps) {
        return new Resultado(eps.getId(), eps.getCodigo(), eps.getNombre(), eps.isActiva());
    }
}
