package com.fcv.citas.domain.model;

import com.fcv.citas.domain.exception.ValidacionNegocioException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Agregado de dominio para HU-010/HU-011 (RF-07). Sin dependencias de
 * Spring/JPA. Un profesional siempre está respaldado por una cuenta
 * {@link Usuario} con rol PROFESSIONAL (ver {@code Usuario.registrarProfesional}).
 */
public final class Profesional {

    private final Long id;
    private final Long usuarioId;
    private final String codigoProfesional;
    private final String matricula;
    private final boolean activo;
    private final Set<AsignacionEspecialidad> especialidades;
    private final Set<Long> sedeIds;

    private Profesional(Long id, Long usuarioId, String codigoProfesional, String matricula, boolean activo,
                         Set<AsignacionEspecialidad> especialidades, Set<Long> sedeIds) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.codigoProfesional = codigoProfesional;
        this.matricula = matricula;
        this.activo = activo;
        this.especialidades = especialidades;
        this.sedeIds = sedeIds;
    }

    /**
     * CA-01/CA-02: crea el profesional con al menos una especialidad (exactamente
     * una primaria) y al menos una sede. La validación de que las especialidades
     * estén activas y las sedes existan es responsabilidad del caso de uso
     * (necesita consultar los repositorios correspondientes).
     */
    public static Profesional registrar(Long usuarioId, String codigoProfesional, String matricula,
                                         Set<AsignacionEspecialidad> especialidades, Set<Long> sedeIds) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
        requerirNoVacio(codigoProfesional, "codigoProfesional");
        requerirNoVacio(matricula, "matricula");
        if (especialidades == null || especialidades.isEmpty()) {
            throw new ValidacionNegocioException("El profesional debe tener al menos una especialidad");
        }
        long primarias = especialidades.stream().filter(AsignacionEspecialidad::primaria).count();
        if (primarias != 1) {
            throw new ValidacionNegocioException("El profesional debe tener exactamente una especialidad primaria");
        }
        if (sedeIds == null || sedeIds.isEmpty()) {
            throw new ValidacionNegocioException("El profesional debe tener al menos una sede");
        }
        return new Profesional(null, usuarioId, codigoProfesional.trim(), matricula.trim(), true,
            new LinkedHashSet<>(especialidades), new LinkedHashSet<>(sedeIds));
    }

    public static Profesional reconstruir(Long id, Long usuarioId, String codigoProfesional, String matricula,
                                           boolean activo, Set<AsignacionEspecialidad> especialidades,
                                           Set<Long> sedeIds) {
        return new Profesional(id, usuarioId, codigoProfesional, matricula, activo, especialidades, sedeIds);
    }

    /** HU-011 CA-01/CA-02: activa o desactiva; no cambia especialidades/sedes. */
    public Profesional cambiarEstado(boolean nuevoActivo) {
        return new Profesional(id, usuarioId, codigoProfesional, matricula, nuevoActivo, especialidades, sedeIds);
    }

    public boolean tieneSedeHabilitada(Long sedeId) {
        return sedeIds.contains(sedeId);
    }

    public boolean tieneEspecialidadActiva(Long especialidadId) {
        return especialidades.stream().anyMatch(a -> a.especialidadId().equals(especialidadId));
    }

    private static void requerirNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getCodigoProfesional() {
        return codigoProfesional;
    }

    public String getMatricula() {
        return matricula;
    }

    public boolean isActivo() {
        return activo;
    }

    public Set<AsignacionEspecialidad> getEspecialidades() {
        return especialidades;
    }

    public Set<Long> getSedeIds() {
        return sedeIds;
    }
}
