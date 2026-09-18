package com.fcv.citas.infrastructure.adapter.out.security;

import com.fcv.citas.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** PRD sección 8: hash adaptativo compatible con Spring Security. */
@Component
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean coincide(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
