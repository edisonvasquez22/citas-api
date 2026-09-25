package com.fcv.citas.domain.model;

/**
 * Catálogo fijo `locations` (HIC/ICV, precargado por HU-006/V2). Solo lectura:
 * no hay HU aprobada que administre sedes todavía.
 */
public record Sede(Long id, String codigo, String nombre, boolean activa) {
}
