package com.fcv.citas.application.port.in;

/** HU-004 — Consultar y actualizar perfil propio (RF-04, parte de perfil). */
public interface GestionarPerfilUseCase {

    Resultado consultar(String usuarioId);

    Resultado actualizar(String usuarioId, ActualizarCommand command);

    record ActualizarCommand(String nombres, String apellidos, String telefono) {}

    record Resultado(String id, String nombres, String apellidos, String tipoDocumento, String numeroDocumento,
                      String email, String telefono) {}
}
