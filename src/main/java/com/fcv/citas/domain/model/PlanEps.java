package com.fcv.citas.domain.model;

/**
 * Agregado de dominio para HU-008 (RF-06). Cada plan pertenece a exactamente
 * una EPS y a un régimen del catálogo fijo `insurance_regimes` (RF-05,
 * precargado por HU-006). Sin dependencias de Spring/JPA.
 */
public final class PlanEps {

    private final Long id;
    private final Long epsId;
    private final Long regimenId;
    private final String codigo;
    private final String nombre;
    private final boolean activo;

    private PlanEps(Long id, Long epsId, Long regimenId, String codigo, String nombre, boolean activo) {
        this.id = id;
        this.epsId = epsId;
        this.regimenId = regimenId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activo = activo;
    }

    public static PlanEps crear(Long epsId, Long regimenId, String codigo, String nombre) {
        requerirNoNulo(epsId, "epsId");
        requerirNoNulo(regimenId, "regimenId");
        requerirNoVacio(codigo, "codigo");
        requerirNoVacio(nombre, "nombre");
        return new PlanEps(null, epsId, regimenId, codigo.trim(), nombre.trim(), true);
    }

    public static PlanEps reconstruir(Long id, Long epsId, Long regimenId, String codigo, String nombre,
                                       boolean activo) {
        return new PlanEps(id, epsId, regimenId, codigo, nombre, activo);
    }

    public PlanEps editar(String nombre) {
        requerirNoVacio(nombre, "nombre");
        return new PlanEps(id, epsId, regimenId, codigo, nombre.trim(), activo);
    }

    public PlanEps cambiarEstado(boolean nuevoActivo) {
        return new PlanEps(id, epsId, regimenId, codigo, nombre, nuevoActivo);
    }

    private static void requerirNoNulo(Object valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser nulo");
        }
    }

    private static void requerirNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getEpsId() {
        return epsId;
    }

    public Long getRegimenId() {
        return regimenId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActivo() {
        return activo;
    }
}
