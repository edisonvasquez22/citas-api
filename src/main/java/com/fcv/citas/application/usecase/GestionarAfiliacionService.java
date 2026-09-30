package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.GestionarAfiliacionUseCase;
import com.fcv.citas.application.port.out.AfiliacionRepositoryPort;
import com.fcv.citas.application.port.out.EpsRepositoryPort;
import com.fcv.citas.application.port.out.PlanEpsRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.Afiliacion;
import com.fcv.citas.domain.model.Eps;
import com.fcv.citas.domain.model.PlanEps;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-005: asocia EPS+plan+carné al usuario autenticado, reemplazando la afiliación vigente previa si existe. */
@Service
public class GestionarAfiliacionService implements GestionarAfiliacionUseCase {

    private final AfiliacionRepositoryPort afiliacionRepository;
    private final EpsRepositoryPort epsRepository;
    private final PlanEpsRepositoryPort planRepository;

    public GestionarAfiliacionService(AfiliacionRepositoryPort afiliacionRepository, EpsRepositoryPort epsRepository,
                                       PlanEpsRepositoryPort planRepository) {
        this.afiliacionRepository = afiliacionRepository;
        this.epsRepository = epsRepository;
        this.planRepository = planRepository;
    }

    @Override
    public Optional<Resultado> consultarVigente(String usuarioId) {
        return afiliacionRepository.buscarVigentePorUsuario(Long.valueOf(usuarioId))
            .map(afiliacion -> {
                PlanEps plan = obtenerPlan(afiliacion.getPlanId());
                Eps eps = epsRepository.buscarPorId(plan.getEpsId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("EPS no encontrada: " + plan.getEpsId()));
                return aResultado(afiliacion, eps, plan);
            });
    }

    @Override
    @Transactional
    public Resultado asociar(String usuarioId, AsociarCommand command) {
        Eps eps = epsRepository.buscarPorId(command.epsId())
            .orElseThrow(() -> new RecursoNoEncontradoException("EPS no encontrada: " + command.epsId()));
        if (!eps.isActiva()) {
            throw new ValidacionNegocioException("La EPS seleccionada está inactiva");
        }
        PlanEps plan = planRepository.buscarPorId(command.planId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Plan de EPS no encontrado: " + command.planId()));
        if (!plan.getEpsId().equals(eps.getId())) {
            throw new ValidacionNegocioException("El plan seleccionado no pertenece a la EPS indicada");
        }
        if (!plan.isActivo()) {
            throw new ValidacionNegocioException("El plan seleccionado está inactivo");
        }

        Long usuarioIdNumerico = Long.valueOf(usuarioId);
        String numero = Afiliacion.crear(usuarioIdNumerico, plan.getId(), command.numeroAfiliacion())
            .getNumeroAfiliacion();
        Optional<Afiliacion> vigente = afiliacionRepository.buscarVigentePorUsuario(usuarioIdNumerico);
        if (vigente.isPresent() && vigente.get().corresponde(plan.getId(), numero)) {
            return aResultado(vigente.get(), eps, plan);
        }
        vigente.ifPresent(previa -> afiliacionRepository.guardar(previa.cerrar()));

        Afiliacion nueva = afiliacionRepository.buscarPorUsuarioPlanYNumero(usuarioIdNumerico, plan.getId(), numero)
            .map(Afiliacion::reactivar)
            .orElseGet(() -> Afiliacion.crear(usuarioIdNumerico, plan.getId(), numero));
        return aResultado(afiliacionRepository.guardar(nueva), eps, plan);
    }

    private PlanEps obtenerPlan(Long planId) {
        return planRepository.buscarPorId(planId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Plan de EPS no encontrado: " + planId));
    }

    private Resultado aResultado(Afiliacion afiliacion, Eps eps, PlanEps plan) {
        return new Resultado(afiliacion.getId(), eps.getId(), eps.getNombre(), plan.getId(), plan.getNombre(),
            plan.getRegimenId(), afiliacion.getNumeroAfiliacion());
    }
}
