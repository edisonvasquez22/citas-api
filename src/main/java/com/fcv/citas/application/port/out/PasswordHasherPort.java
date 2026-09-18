package com.fcv.citas.application.port.out;

/** RF-01/PRD sección 8: hash adaptativo, nunca texto plano. */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean coincide(String rawPassword, String hash);
}
