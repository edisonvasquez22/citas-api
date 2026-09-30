package com.fcv.citas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fcv.citas.application.port.in.ConsultarCitasIntegracionUseCase.ResumenDiario;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.CitaNotificable;
import com.fcv.citas.testsupport.InMemoryConsultaCitasIntegracionAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsultarCitasIntegracionServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 10, 5);

    private InMemoryConsultaCitasIntegracionAdapter consulta;
    private ConsultarCitasIntegracionService service;

    @BeforeEach
    void setUp() {
        consulta = new InMemoryConsultaCitasIntegracionAdapter();
        service = new ConsultarCitasIntegracionService(consulta);
        consulta.agregar(cita(1L, "APPROVED", DIA.atTime(8, 0), "HIC", "Medicina General"));
        consulta.agregar(cita(2L, "CANCELLED", DIA.atTime(9, 0), "HIC", "Medicina General"));
        consulta.agregar(cita(3L, "REQUESTED", DIA.atTime(10, 0), "ICV", "Cardiología Adulto"));
        consulta.agregar(cita(4L, "APPROVED", DIA.plusDays(1).atTime(8, 0), "ICV", "Neurología"));
    }

    @Test
    void recordatoriosSoloIncluyeAprobadasDentroDeLaVentana() {
        var resultado = service.citasParaRecordatorio(DIA.atStartOfDay(), DIA.plusDays(1).atStartOfDay());

        assertThat(resultado).extracting(CitaNotificable::citaId).containsExactly(1L);
    }

    @Test
    void recordatoriosRechazaVentanaInvertidaOMayorASieteDias() {
        assertThatThrownBy(() -> service.citasParaRecordatorio(DIA.atTime(10, 0), DIA.atTime(9, 0)))
            .isInstanceOf(ValidacionNegocioException.class);
        assertThatThrownBy(() -> service.citasParaRecordatorio(DIA.atStartOfDay(), DIA.plusDays(8).atStartOfDay()))
            .isInstanceOf(ValidacionNegocioException.class);
    }

    @Test
    void resumenDiarioAgrupaPorEstadoSedeYEspecialidad() {
        ResumenDiario resumen = service.resumenDiario(DIA);

        assertThat(resumen.total()).isEqualTo(3);
        assertThat(resumen.porEstado()).containsEntry("APPROVED", 1L).containsEntry("CANCELLED", 1L)
            .containsEntry("REQUESTED", 1L);
        assertThat(resumen.porSede()).containsEntry("HIC", 2L).containsEntry("ICV", 1L);
        assertThat(resumen.porEspecialidad()).containsEntry("Medicina General", 2L);
    }

    private static CitaNotificable cita(Long id, String estado, LocalDateTime inicio, String sede,
                                        String especialidad) {
        return new CitaNotificable(id, estado, inicio, inicio.plusMinutes(30), 100L, "Paciente Uno",
            "paciente1@demo.invalid", "Andrea Ruiz", sede, sede + " nombre", especialidad);
    }
}
