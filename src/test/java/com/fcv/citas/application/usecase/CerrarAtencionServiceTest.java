package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.testsupport.InMemoryCitaRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryHistorialEstadoCitaAdapter;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-022: CA-01 a CA-03 (marcar completada, marcar no asistida, sin ownership). */
class CerrarAtencionServiceTest {

    private InMemoryCitaRepositoryAdapter citaRepository;
    private CerrarAtencionService service;
    private Long usuarioProfesional;
    private Long usuarioOtroProfesional;
    private Cita citaPasadaAprobada;

    @BeforeEach
    void setUp() {
        citaRepository = new InMemoryCitaRepositoryAdapter();
        InMemoryProfesionalRepositoryAdapter profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        service = new CerrarAtencionService(citaRepository, profesionalRepository,
            new InMemoryHistorialEstadoCitaAdapter());

        usuarioProfesional = 50L;
        Profesional profesional = profesionalRepository.guardar(Profesional.registrar(usuarioProfesional, "PROF-A",
            "MAT-A", Set.of(new AsignacionEspecialidad(1L, true)), Set.of(1L)));

        usuarioOtroProfesional = 51L;
        profesionalRepository.guardar(Profesional.registrar(usuarioOtroProfesional, "PROF-B", "MAT-B",
            Set.of(new AsignacionEspecialidad(1L, true)), Set.of(1L)));

        LocalDateTime inicioPasado = LocalDateTime.now().minusHours(2);
        citaPasadaAprobada = citaRepository.guardar(Cita.solicitarGeneral(1L, profesional.getId(), 1L, 1L, null,
            inicioPasado, inicioPasado.plusMinutes(30)));
    }

    @Test
    void completar_citaPropiaAprobadaYPasada_pasaACompleted() {
        var resultado = service.completar(usuarioProfesional, citaPasadaAprobada.getId());

        assertThat(resultado.estado()).isEqualTo("COMPLETED");
        assertThat(citaRepository.buscarPorId(citaPasadaAprobada.getId())).get().extracting(Cita::getEstado)
            .isEqualTo(EstadoCita.COMPLETED);
    }

    @Test
    void marcarNoShow_citaPropiaAprobadaYPasada_pasaANoShow() {
        var resultado = service.marcarNoShow(usuarioProfesional, citaPasadaAprobada.getId());

        assertThat(resultado.estado()).isEqualTo("NO_SHOW");
    }

    @Test
    void completar_citaFutura_seRechaza() {
        LocalDateTime inicioFuturo = LocalDateTime.now().plusDays(1);
        Cita citaFutura = citaRepository.guardar(Cita.solicitarGeneral(1L,
            citaPasadaAprobada.getProfesionalId(), 1L, 1L, null, inicioFuturo, inicioFuturo.plusMinutes(30)));

        assertThatThrownBy(() -> service.completar(usuarioProfesional, citaFutura.getId()))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void completar_citaDeOtroProfesional_seRechazaComoNoEncontrada() {
        assertThatThrownBy(() -> service.completar(usuarioOtroProfesional, citaPasadaAprobada.getId()))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
