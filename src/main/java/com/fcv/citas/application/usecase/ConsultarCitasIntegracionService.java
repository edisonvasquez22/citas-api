package com.fcv.citas.application.usecase;

import com.fcv.citas.application.port.in.ConsultarCitasIntegracionUseCase;
import com.fcv.citas.application.port.out.ConsultaCitasIntegracionPort;
import com.fcv.citas.domain.exception.ValidacionNegocioException;
import com.fcv.citas.domain.model.CitaNotificable;
import com.fcv.citas.domain.model.EstadoCita;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ConsultarCitasIntegracionService implements ConsultarCitasIntegracionUseCase {

    private static final Duration VENTANA_MAXIMA = Duration.ofDays(7);

    private final ConsultaCitasIntegracionPort consultaPort;

    public ConsultarCitasIntegracionService(ConsultaCitasIntegracionPort consultaPort) {
        this.consultaPort = consultaPort;
    }

    @Override
    public List<CitaNotificable> citasParaRecordatorio(LocalDateTime desde, LocalDateTime hasta) {
        if (!hasta.isAfter(desde)) {
            throw new ValidacionNegocioException("'hasta' debe ser posterior a 'desde'");
        }
        if (Duration.between(desde, hasta).compareTo(VENTANA_MAXIMA) > 0) {
            throw new ValidacionNegocioException("La ventana de consulta no puede superar 7 días");
        }
        return consultaPort.listar(EstadoCita.APPROVED.name(), desde, hasta);
    }

    @Override
    public ResumenDiario resumenDiario(LocalDate fecha) {
        List<CitaNotificable> citas = consultaPort.listar(null, fecha.atStartOfDay(),
            fecha.plusDays(1).atStartOfDay());
        return new ResumenDiario(fecha, citas.size(),
            contarPor(citas, CitaNotificable::estado),
            contarPor(citas, CitaNotificable::sedeCodigo),
            contarPor(citas, CitaNotificable::especialidadNombre),
            citas);
    }

    private static Map<String, Long> contarPor(List<CitaNotificable> citas,
                                               Function<CitaNotificable, String> clave) {
        return citas.stream().collect(Collectors.groupingBy(clave, TreeMap::new, Collectors.counting()));
    }
}
