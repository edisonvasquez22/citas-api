package com.fcv.citas.domain.model;

/**
 * Agregado de dominio para HU-007 (RF-06). Sin dependencias de Spring/JPA.
 * El id es {@code null} hasta que se persiste (BIGINT UNSIGNED autoincremental
 * asignado por MySQL).
 */
public final class Eps {

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final boolean activa;

    private Eps(Long id, String codigo, String nombre, boolean activa) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activa = activa;
    }

    public static Eps crear(String codigo, String nombre) {
        requerirNoVacio(codigo, "codigo");
        requerirNoVacio(nombre, "nombre");
        return new Eps(null, codigo.trim(), nombre.trim(), true);
    }

    public static Eps reconstruir(Long id, String codigo, String nombre, boolean activa) {
        return new Eps(id, codigo, nombre, activa);
    }

    public Eps editar(String nombre) {
        requerirNoVacio(nombre, "nombre");
        return new Eps(id, codigo, nombre.trim(), activa);
    }

    public Eps cambiarEstado(boolean nuevaActiva) {
        return new Eps(id, codigo, nombre, nuevaActiva);
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

    public boolean isActiva() {
        return activa;
    }
}
