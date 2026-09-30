package com.fcv.citas.domain.model;

import java.time.LocalDate;

/**
 * Agregado de dominio para HU-005 (RF-04, parte de afiliación). Un usuario
 * tiene como máximo una afiliación vigente (`vigente = true`) a la vez; ver
 * GestionarAfiliacionService para la regla de reemplazo. Sin dependencias de
 * Spring/JPA.
 */
public final class Afiliacion {

    private final Long id;
    private final Long usuarioId;
    private final Long planId;
    private final String numeroAfiliacion;
    private final boolean vigente;
    private final LocalDate vigenteDesde;

    private Afiliacion(Long id, Long usuarioId, Long planId, String numeroAfiliacion, boolean vigente,
                        LocalDate vigenteDesde) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.planId = planId;
        this.numeroAfiliacion = numeroAfiliacion;
        this.vigente = vigente;
        this.vigenteDesde = vigenteDesde;
    }

    public static Afiliacion crear(Long usuarioId, Long planId, String numeroAfiliacion) {
        requerirNoNulo(usuarioId, "usuarioId");
        requerirNoNulo(planId, "planId");
        requerirNoVacio(numeroAfiliacion, "numeroAfiliacion");
        return new Afiliacion(null, usuarioId, planId, numeroAfiliacion.trim(), true, LocalDate.now());
    }

    public static Afiliacion reconstruir(Long id, Long usuarioId, Long planId, String numeroAfiliacion,
                                          boolean vigente, LocalDate vigenteDesde) {
        return new Afiliacion(id, usuarioId, planId, numeroAfiliacion, vigente, vigenteDesde);
    }

    /** RF-04: al asociar una nueva afiliación, la anterior deja de estar vigente (no se borra, queda de historial). */
    public Afiliacion cerrar() {
        return new Afiliacion(id, usuarioId, planId, numeroAfiliacion, false, vigenteDesde);
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getNumeroAfiliacion() {
        return numeroAfiliacion;
    }

    public boolean isVigente() {
        return vigente;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }
}
