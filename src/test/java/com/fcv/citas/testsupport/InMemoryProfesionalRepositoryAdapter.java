package com.fcv.citas.testsupport;

import com.fcv.citas.application.port.out.ProfesionalRepositoryPort;
import com.fcv.citas.domain.model.Profesional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Doble de prueba de {@link ProfesionalRepositoryPort} (HU-010/HU-011), sin MySQL. */
public class InMemoryProfesionalRepositoryAdapter implements ProfesionalRepositoryPort {

    private final AtomicLong secuenciaId = new AtomicLong(0);
    private final Map<Long, Profesional> porId = new ConcurrentHashMap<>();
    private final Map<String, Long> idPorCodigo = new ConcurrentHashMap<>();
    private final Map<String, Long> idPorMatricula = new ConcurrentHashMap<>();
    private final Map<Long, Long> idPorUsuarioId = new ConcurrentHashMap<>();

    @Override
    public boolean existeCodigoProfesional(String codigoProfesional) {
        return codigoProfesional != null && idPorCodigo.containsKey(codigoProfesional);
    }

    @Override
    public boolean existeMatricula(String matricula) {
        return matricula != null && idPorMatricula.containsKey(matricula);
    }

    @Override
    public synchronized Profesional guardar(Profesional profesional) {
        Profesional aGuardar = profesional;
        if (aGuardar.getId() == null) {
            Long nuevoId = secuenciaId.incrementAndGet();
            aGuardar = Profesional.reconstruir(nuevoId, profesional.getUsuarioId(), profesional.getCodigoProfesional(),
                profesional.getMatricula(), profesional.isActivo(), profesional.getEspecialidades(),
                profesional.getSedeIds());
        }
        porId.put(aGuardar.getId(), aGuardar);
        idPorCodigo.put(aGuardar.getCodigoProfesional(), aGuardar.getId());
        idPorMatricula.put(aGuardar.getMatricula(), aGuardar.getId());
        idPorUsuarioId.put(aGuardar.getUsuarioId(), aGuardar.getId());
        return aGuardar;
    }

    @Override
    public Optional<Profesional> buscarPorId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public Optional<Profesional> buscarPorUsuarioId(Long usuarioId) {
        return Optional.ofNullable(idPorUsuarioId.get(usuarioId)).map(porId::get);
    }

    @Override
    public List<Profesional> listarActivos() {
        return porId.values().stream().filter(Profesional::isActivo).toList();
    }
}
