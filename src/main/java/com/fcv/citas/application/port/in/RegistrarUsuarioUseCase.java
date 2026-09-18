package com.fcv.citas.application.port.in;

/** HU-001 — Registrar cuenta de usuario. */
public interface RegistrarUsuarioUseCase {

    Resultado registrar(Command command);

    record Command(
        String nombres,
        String apellidos,
        String tipoDocumento,
        String numeroDocumento,
        String email,
        String telefono,
        String password
    ) {}

    record Resultado(String usuarioId, String email) {}
}
