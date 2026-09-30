package com.fcv.citas.application.port.in;

/** Crea la cuenta ADMIN inicial solo si todavía no existe ningún ADMIN (no hay autoregistro de administradores). */
public interface CrearAdministradorInicialUseCase {

    Resultado crearSiNoExiste(Command command);

    record Command(String email, String password, String nombres, String apellidos, String numeroDocumento,
                   String telefono) {}

    enum Resultado { CREADO, YA_EXISTE_ADMIN, EMAIL_EN_USO }
}
