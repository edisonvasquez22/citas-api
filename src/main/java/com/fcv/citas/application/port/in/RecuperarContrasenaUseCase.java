package com.fcv.citas.application.port.in;

/** HU-003 — Recuperar contraseña con token temporal de un solo uso (RF-03). */
public interface RecuperarContrasenaUseCase {

    /** CA-01: nunca revela si el email existe; el llamador siempre responde el mismo mensaje genérico. */
    void solicitar(String email);

    /** CA-02/CA-03: valida y consume el token, o rechaza sin modificar la contraseña. */
    void confirmar(String tokenPlano, String nuevaPassword);
}
