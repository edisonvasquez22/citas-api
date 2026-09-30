package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.model.AsignacionEspecialidad;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.domain.model.FuenteCambioEstado;
import com.fcv.citas.domain.model.Profesional;
import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import com.fcv.citas.testsupport.InMemoryCitaRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryHistorialEstadoCitaAdapter;
import com.fcv.citas.testsupport.InMemoryProfesionalRepositoryAdapter;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-023 CA-03: historial visible solo dentro del ownership del rol. */
class ConsultarHistorialCitaServiceTest {

    private static final Long PACIENTE = 100L;
    private static final Long OTRO_PACIENTE = 101L;
    private static final Long USUARIO_PROFESIONAL = 50L;
    private static final Long USUARIO_OTRO_PROFESIONAL = 51L;

    private ConsultarHistorialCitaService service;
    private Cita cita;

    @BeforeEach
    void setUp() {
        InMemoryCitaRepositoryAdapter citaRepository = new InMemoryCitaRepositoryAdapter();
        InMemoryProfesionalRepositoryAdapter profesionalRepository = new InMemoryProfesionalRepositoryAdapter();
        InMemoryHistorialEstadoCitaAdapter historial = new InMemoryHistorialEstadoCitaAdapter();
        service = new ConsultarHistorialCitaService(citaRepository, profesionalRepository, historial);

        Profesional profesional = profesionalRepository.guardar(Profesional.registrar(USUARIO_PROFESIONAL, "PROF-A",
            "MAT-A", Set.of(new AsignacionEspecialidad(1L, true)), Set.of(1L)));
        profesionalRepository.guardar(Profesional.registrar(USUARIO_OTRO_PROFESIONAL, "PROF-B", "MAT-B",
            Set.of(new AsignacionEspecialidad(1L, true)), Set.of(1L)));

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        cita = citaRepository.guardar(Cita.solicitarGeneral(PACIENTE, profesional.getId(), 1L, 1L, null, inicio,
            inicio.plusMinutes(30)));
        historial.registrar(TransicionEstadoCita.nueva(cita.getId(), EstadoCita.APPROVED, null,
            FuenteCambioEstado.SYSTEM, "Aprobación automática", LocalDateTime.now()));
    }

    @Test
    void pacienteDuenoVeElHistorial() {
        assertThat(service.consultar(PACIENTE, Set.of(RolNombre.USER), cita.getId()))
            .extracting(TransicionEstadoCita::estado).containsExactly(EstadoCita.APPROVED);
    }

    @Test
    void profesionalDeLaCitaVeElHistorial() {
        assertThat(service.consultar(USUARIO_PROFESIONAL, Set.of(RolNombre.PROFESSIONAL), cita.getId())).hasSize(1);
    }

    @Test
    void adminVeCualquierHistorial() {
        assertThat(service.consultar(1L, Set.of(RolNombre.ADMIN), cita.getId())).hasSize(1);
    }

    @Test
    void otroPacienteRecibe404() {
        assertThatThrownBy(() -> service.consultar(OTRO_PACIENTE, Set.of(RolNombre.USER), cita.getId()))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void otroProfesionalRecibe404() {
        assertThatThrownBy(() -> service.consultar(USUARIO_OTRO_PROFESIONAL, Set.of(RolNombre.PROFESSIONAL),
            cita.getId())).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void citaInexistenteRecibe404() {
        assertThatThrownBy(() -> service.consultar(PACIENTE, Set.of(RolNombre.USER), 999L))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
