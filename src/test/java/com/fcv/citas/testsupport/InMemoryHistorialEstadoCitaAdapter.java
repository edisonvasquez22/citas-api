package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.HistorialEstadoCitaPort;
import com.fcv.citas.domain.model.TransicionEstadoCita;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de prueba de {@link HistorialEstadoCitaPort} (HU-023): solo agrega, nunca edita/borra. */
public class InMemoryHistorialEstadoCitaAdapter implements HistorialEstadoCitaPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final List<TransicionEstadoCita> transiciones = new CopyOnWriteArrayList<>();

    @Override
    public void registrar(TransicionEstadoCita transicion) {
        long id = secuenciaId.incrementAndGet();
        transiciones.add(new TransicionEstadoCita(id, transicion.citaId(), transicion.estado(),
            transicion.actorUsuarioId(), transicion.fuente(), transicion.motivo(), transicion.momento()));
    }

    @Override
    public List<TransicionEstadoCita> listarPorCita(Long citaId) {
        return transiciones.stream().filter(t -> t.citaId().equals(citaId)).toList();
    }
}
