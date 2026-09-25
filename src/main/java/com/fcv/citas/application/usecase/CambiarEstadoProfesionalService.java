package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.CambiarEstadoProfesionalUseCase;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.Profesional;
import org.springframework.stereotype.Service;

@Service
public class CambiarEstadoProfesionalService implements CambiarEstadoProfesionalUseCase {

    private final ProfesionalRepositoryPort profesionalRepository;

    public CambiarEstadoProfesionalService(ProfesionalRepositoryPort profesionalRepository) {
        this.profesionalRepository = profesionalRepository;
    }

    @Override
    public Resultado cambiarEstado(Long profesionalId, boolean activo) {
        Profesional profesional = profesionalRepository.buscarPorId(profesionalId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado: " + profesionalId));
        Profesional actualizado = profesionalRepository.guardar(profesional.cambiarEstado(activo));
        return new Resultado(actualizado.getId(), actualizado.isActivo());
    }
}
