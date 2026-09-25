package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.ConsultarDisponibilidadUseCase;
import com.fcv.citas.application.port.out.EspecialidadRepositoryPort;
import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.application.port.out.SlotRepositoryPort;
import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.Especialidad;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.SlotProfesional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * HU-013: combina filtros de sede/profesional/fecha con la especialidad
 * elegida (define cuántos slots consecutivos hacen falta, RN-05) para exponer
 * solo horarios efectivamente reservables (RF-10, RN-01: excluye lo retenido).
 */
@Service
public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    private final EspecialidadRepositoryPort especialidadRepository;
    private final ProfesionalRepositoryPort profesionalRepository;
    private final SlotRepositoryPort slotRepository;

    public ConsultarDisponibilidadService(EspecialidadRepositoryPort especialidadRepository,
                                           ProfesionalRepositoryPort profesionalRepository,
                                           SlotRepositoryPort slotRepository) {
        this.especialidadRepository = especialidadRepository;
        this.profesionalRepository = profesionalRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    public List<HorarioDisponible> consultar(Consulta consulta) {
        if (consulta.especialidadId() == null || consulta.fecha() == null) {
            throw new ValidacionNegocioException("especialidadId y fecha son obligatorios para consultar disponibilidad");
        }
        Especialidad especialidad = especialidadRepository.buscarPorId(consulta.especialidadId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada: " + consulta.especialidadId()));
        if (!especialidad.isActiva()) {
            throw new ValidacionNegocioException("La especialidad consultada está inactiva");
        }
        int slotsNecesarios = especialidad.getDuracionMinutos() / com.fcv.citas.domain.model.BloqueDisponibilidad.DURACION_SLOT_MINUTOS;

        Set<Long> candidatos = profesionalRepository.listarActivos().stream()
            .filter(p -> p.tieneEspecialidadActiva(consulta.especialidadId()))
            .filter(p -> consulta.sedeId() == null || p.tieneSedeHabilitada(consulta.sedeId()))
            .filter(p -> consulta.profesionalId() == null || consulta.profesionalId().equals(p.getId()))
            .map(Profesional::getId)
            .collect(Collectors.toSet());
        if (candidatos.isEmpty()) {
            return List.of();
        }

        List<SlotProfesional> libres = slotRepository.buscarLibres(candidatos, consulta.sedeId(), consulta.fecha());

        Map<String, List<SlotProfesional>> porProfesionalYSede = new LinkedHashMap<>();
        for (SlotProfesional slot : libres) {
            String clave = slot.profesionalId() + ":" + slot.sedeId();
            porProfesionalYSede.computeIfAbsent(clave, k -> new ArrayList<>()).add(slot);
        }

        List<HorarioDisponible> resultado = new ArrayList<>();
        for (List<SlotProfesional> grupo : porProfesionalYSede.values()) {
            grupo.sort(Comparator.comparing(SlotProfesional::inicio));
            resultado.addAll(ventanasReservables(grupo, slotsNecesarios));
        }
        return resultado;
    }

    /** RN-05: una ventana solo es reservable si sus slots son consecutivos y libres (CA-02). */
    private List<HorarioDisponible> ventanasReservables(List<SlotProfesional> slotsOrdenados, int slotsNecesarios) {
        List<HorarioDisponible> ventanas = new ArrayList<>();
        for (int i = 0; i + slotsNecesarios <= slotsOrdenados.size(); i++) {
            boolean contiguos = true;
            for (int k = i; k < i + slotsNecesarios - 1; k++) {
                if (!slotsOrdenados.get(k).fin().equals(slotsOrdenados.get(k + 1).inicio())) {
                    contiguos = false;
                    break;
                }
            }
            if (contiguos) {
                SlotProfesional primero = slotsOrdenados.get(i);
                SlotProfesional ultimo = slotsOrdenados.get(i + slotsNecesarios - 1);
                ventanas.add(new HorarioDisponible(primero.profesionalId(), primero.sedeId(), primero.inicio(),
                    ultimo.fin()));
            }
        }
        return ventanas;
    }
}
