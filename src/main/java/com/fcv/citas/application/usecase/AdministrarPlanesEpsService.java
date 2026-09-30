package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.AdministrarPlanesEpsUseCase;
import com.fcv.citas.application.port.out.EpsRepositoryPort;
import com.fcv.citas.application.port.out.PlanEpsRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.PlanEps;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdministrarPlanesEpsService implements AdministrarPlanesEpsUseCase {

    private final PlanEpsRepositoryPort planRepository;
    private final EpsRepositoryPort epsRepository;

    public AdministrarPlanesEpsService(PlanEpsRepositoryPort planRepository, EpsRepositoryPort epsRepository) {
        this.planRepository = planRepository;
        this.epsRepository = epsRepository;
    }

    @Override
    public Resultado crear(Long epsId, CrearCommand command) {
        epsRepository.buscarPorId(epsId)
            .orElseThrow(() -> new RecursoNoEncontradoException("EPS no encontrada: " + epsId));
        if (planRepository.existePorEpsYCodigo(epsId, command.codigo())) {
            throw new ValidacionNegocioException(
                "Ya existe un plan con el código '" + command.codigo() + "' para esa EPS");
        }
        PlanEps plan = PlanEps.crear(epsId, command.regimenId(), command.codigo(), command.nombre());
        return aResultado(planRepository.guardar(plan));
    }

    @Override
    public Resultado editar(Long id, EditarCommand command) {
        PlanEps actual = obtener(id);
        return aResultado(planRepository.guardar(actual.editar(command.nombre())));
    }

    @Override
    public Resultado cambiarEstado(Long id, boolean activo) {
        PlanEps actual = obtener(id);
        return aResultado(planRepository.guardar(actual.cambiarEstado(activo)));
    }

    @Override
    public List<Resultado> listarPorEps(Long epsId) {
        return planRepository.listarPorEps(epsId).stream().map(this::aResultado).toList();
    }

    private PlanEps obtener(Long id) {
        return planRepository.buscarPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Plan de EPS no encontrado: " + id));
    }

    private Resultado aResultado(PlanEps plan) {
        return new Resultado(plan.getId(), plan.getEpsId(), plan.getRegimenId(), plan.getCodigo(), plan.getNombre(),
            plan.isActivo());
    }
}
