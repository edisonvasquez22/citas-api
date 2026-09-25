package com.fcv.citas.domain.model;

import java.util.Set;

/**
 * Agregado de dominio para HU-009 (RF-06/RF-09). Sin dependencias de
 * Spring/JPA. El id es {@code null} hasta que se persiste (SMALLINT
 * autoincremental asignado por MySQL).
 */
public final class Especialidad {

    private static final Set<Integer> DURACIONES_VALIDAS = Set.of(30, 60);

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final int duracionMinutos;
    private final boolean general;
    private final boolean requiereAprobacionAdmin;
    private final boolean activa;

    private Especialidad(Long id, String codigo, String nombre, int duracionMinutos, boolean general,
                          boolean requiereAprobacionAdmin, boolean activa) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.duracionMinutos = duracionMinutos;
        this.general = general;
        this.requiereAprobacionAdmin = requiereAprobacionAdmin;
        this.activa = activa;
    }

    /** CA-01/CA-02: la duración solo puede ser 30 o 60 minutos. */
    public static Especialidad crear(String codigo, String nombre, int duracionMinutos, boolean general,
                                      boolean requiereAprobacionAdmin) {
        requerirNoVacio(codigo, "codigo");
        requerirNoVacio(nombre, "nombre");
        requerirDuracionValida(duracionMinutos);
        return new Especialidad(null, codigo.trim(), nombre.trim(), duracionMinutos, general,
            requiereAprobacionAdmin, true);
    }

    public static Especialidad reconstruir(Long id, String codigo, String nombre, int duracionMinutos,
                                            boolean general, boolean requiereAprobacionAdmin, boolean activa) {
        return new Especialidad(id, codigo, nombre, duracionMinutos, general, requiereAprobacionAdmin, activa);
    }

    public Especialidad editar(String nombre, int duracionMinutos, boolean general, boolean requiereAprobacionAdmin) {
        requerirNoVacio(nombre, "nombre");
        requerirDuracionValida(duracionMinutos);
        return new Especialidad(id, codigo, nombre.trim(), duracionMinutos, general, requiereAprobacionAdmin, activa);
    }

    public Especialidad cambiarEstado(boolean nuevaActiva) {
        return new Especialidad(id, codigo, nombre, duracionMinutos, general, requiereAprobacionAdmin, nuevaActiva);
    }

    private static void requerirDuracionValida(int duracionMinutos) {
        if (!DURACIONES_VALIDAS.contains(duracionMinutos)) {
            throw new com.fcv.citas.domain.exception.ValidacionNegocioException(
                "La duración de la especialidad debe ser 30 o 60 minutos");
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

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public boolean isGeneral() {
        return general;
    }

    public boolean isRequiereAprobacionAdmin() {
        return requiereAprobacionAdmin;
    }

    public boolean isActiva() {
        return activa;
    }
}
