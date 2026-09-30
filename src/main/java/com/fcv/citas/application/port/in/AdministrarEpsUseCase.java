package com.fcv.citas.application.port.in;

import java.util.List;

/** HU-007 — Administrar catálogo de EPS (RF-06). */
public interface AdministrarEpsUseCase {

    Resultado crear(CrearCommand command);

    Resultado editar(Long id, EditarCommand command);

    Resultado cambiarEstado(Long id, boolean activa);

    List<Resultado> listar();

    record CrearCommand(String codigo, String nombre) {}

    record EditarCommand(String nombre) {}

    record Resultado(Long id, String codigo, String nombre, boolean activa) {}
}
