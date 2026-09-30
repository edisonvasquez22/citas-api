package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.domain.exception.RecursoNoEncontradoException;
import com.fcv.citas.domain.exception.TransicionEstadoInvalidaException;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.Cita;
import com.fcv.citas.domain.model.EstadoCita;
import com.fcv.citas.testsupport.InMemoryCitaRepositoryAdapter;
import com.fcv.citas.testsupport.InMemoryDisponibilidadStore;
import com.fcv.citas.testsupport.InMemoryHistorialEstadoCitaAdapter;
import com.fcv.citas.testsupport.InMemorySlotRepositoryAdapter;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-018: CA-01 a CA-03 (cancelación válida, cita terminal, ownership). */
class CancelarCitaServiceTest {

    private InMemoryCitaRepositoryAdapter citaRepository;
    private CancelarCitaService service;

    private static final Long PACIENTE = 1L;
    private static final Long OTRO_PACIENTE = 2L;

    @BeforeEach
    void setUp() {
        citaRepository = new InMemoryCitaRepositoryAdapter();
        InMemorySlotRepositoryAdapter slotRepository = new InMemorySlotRepositoryAdapter(new InMemoryDisponibilidadStore());
        service = new CancelarCitaService(citaRepository, slotRepository, new InMemoryHistorialEstadoCitaAdapter(),
            evento -> { });
    }

    private Cita citaFutura(Long pacienteId) {
        LocalDateTime inicio = LocalDateTime.now().plusDays(5);
        return citaRepository.guardar(Cita.solicitarGeneral(pacienteId, 10L, 1L, 1L, null, inicio, inicio.plusMinutes(30)));
    }

    @Test
    void cancelar_citaPropiaFuturaYNoTerminal_pasaACancelled() {
        Cita cita = citaFutura(PACIENTE);

        var resultado = service.cancelar(PACIENTE, cita.getId());

        assertThat(resultado.estado()).isEqualTo("CANCELLED");
        assertThat(citaRepository.buscarPorId(cita.getId())).get().extracting(Cita::getEstado)
            .isEqualTo(EstadoCita.CANCELLED);
    }

    @Test
    void cancelar_citaYaCancelada_seRechaza() {
        Cita cita = citaFutura(PACIENTE);
        service.cancelar(PACIENTE, cita.getId());

        assertThatThrownBy(() -> service.cancelar(PACIENTE, cita.getId()))
            .isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    void cancelar_citaDeOtroUsuario_seRechazaComoNoEncontrada() {
        Cita cita = citaFutura(PACIENTE);

        assertThatThrownBy(() -> service.cancelar(OTRO_PACIENTE, cita.getId()))
            .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cancelar_citaPasada_seRechaza() {
        LocalDateTime inicioPasado = LocalDateTime.now().minusDays(1);
        Cita cita = citaRepository.guardar(
            Cita.solicitarGeneral(PACIENTE, 10L, 1L, 1L, null, inicioPasado, inicioPasado.plusMinutes(30)));

        assertThatThrownBy(() -> service.cancelar(PACIENTE, cita.getId()))
            .isInstanceOf(ValidacionNegocioException.class);
    }
}
