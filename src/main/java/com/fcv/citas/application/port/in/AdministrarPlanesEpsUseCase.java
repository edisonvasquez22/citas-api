package com.fcv.citas.application.port.in;

import java.util.List;

/** HU-008 — Administrar catálogo de planes de EPS (RF-06). */
public interface AdministrarPlanesEpsUseCase {

    Resultado crear(Long epsId, CrearCommand command);

    Resultado editar(Long id, EditarCommand command);

    Resultado cambiarEstado(Long id, boolean activo);

    List<Resultado> listarPorEps(Long epsId);

    record CrearCommand(Long regimenId, String codigo, String nombre) {}

    record EditarCommand(String nombre) {}

    record Resultado(Long id, Long epsId, Long regimenId, String codigo, String nombre, boolean activo) {}
}
