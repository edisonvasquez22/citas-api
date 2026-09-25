package com.fcv.citas.application.port.in;

import java.util.List;

/** HU-009 — Administrar catálogo de especialidades (RF-06, RF-09). */
public interface AdministrarEspecialidadesUseCase {

    Resultado crear(CrearCommand command);

    Resultado editar(Long id, EditarCommand command);

    Resultado cambiarEstado(Long id, boolean activa);

    List<Resultado> listar();

    record CrearCommand(String codigo, String nombre, int duracionMinutos, boolean general,
                         boolean requiereAprobacionAdmin) {}

    record EditarCommand(String nombre, int duracionMinutos, boolean general, boolean requiereAprobacionAdmin) {}

    record Resultado(Long id, String codigo, String nombre, int duracionMinutos, boolean general,
                      boolean requiereAprobacionAdmin, boolean activa) {}
}
