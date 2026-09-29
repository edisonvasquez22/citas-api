package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.testsupport.InMemoryCitaRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-021: CA-01/CA-02 (agenda propia filtrada, sin acceso a agenda ajena). */
class ConsultarAgendaPropiaServiceTest {

    private InMemoryCitaRepositoryAdapter citaRepository;
    private InMemoryProfesionalRepositoryAdapter profesionalRepository;
    private ConsultarAgendaPropiaService service;

    private Long usuarioProfesionalA;
    private Long profesionalAId;

    @BeforeEach
    void setUp() {
        citaRepository = new InMemoryCitaRepositoryAdapter();
        profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        service = new ConsultarAgendaPropiaService(citaRepository, profesionalRepository);

        usuarioProfesionalA = 50L;
        Profesional profesionalA = profesionalRepository.guardar(Profesional.registrar(usuarioProfesionalA,
            "PROF-A", "MAT-A", Set.of(new AsignacionEspecialidad(1L, true)), Set.of(1L, 2L)));
        profesionalAId = profesionalA.getId();

        Profesional profesionalB = profesionalRepository.guardar(Profesional.registrar(51L, "PROF-B", "MAT-B",
            Set.of(new AsignacionEspecialidad(1L, true)), Set.of(1L)));

        LocalDateTime inicio = LocalDateTime.of(2027, 3, 1, 8, 0);
        // Cita APPROVED del profesional A en sede 1.
        citaRepository.guardar(Cita.solicitarGeneral(1L, profesionalAId, 1L, 1L, null, inicio, inicio.plusMinutes(30)));
        // Cita REQUESTED del profesional A (no debe aparecer, solo APPROVED).
        citaRepository.guardar(Cita.solicitarEspecializada(2L, profesionalAId, 1L, 1L, null,
            inicio.plusHours(1), inicio.plusHours(1).plusMinutes(30)));
        // Cita APPROVED del profesional B (no debe aparecer en la agenda de A).
        citaRepository.guardar(Cita.solicitarGeneral(3L, profesionalB.getId(), 1L, 1L, null, inicio,
            inicio.plusMinutes(30)));
    }

    @Test
    void listar_devuelveSoloAprobadasDelProfesionalPropio() {
        var resultado = service.listar(usuarioProfesionalA, null, null, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).pacienteUsuarioId()).isEqualTo(1L);
    }

    @Test
    void listar_filtraPorSede() {
        LocalDateTime inicio = LocalDateTime.of(2027, 3, 2, 8, 0);
        citaRepository.guardar(Cita.solicitarGeneral(4L, profesionalAId, 2L, 1L, null, inicio, inicio.plusMinutes(30)));

        var soloSede2 = service.listar(usuarioProfesionalA, 2L, null, null);

        assertThat(soloSede2).hasSize(1);
        assertThat(soloSede2.get(0).sedeId()).isEqualTo(2L);
    }
}
