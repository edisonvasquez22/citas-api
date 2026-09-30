package com.fcv.citas.infrastructure.config;

import com.fcv.citas.application.port.in.CrearAdministradorInicialUseCase;
import com.fcv.citas.application.port.in.CrearAdministradorInicialUseCase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Al arrancar, crea el ADMIN inicial desde ADMIN_BOOTSTRAP_EMAIL / ADMIN_BOOTSTRAP_PASSWORD si aún no hay
 * ningún ADMIN. Sin esas variables no hace nada; con un ADMIN ya existente, tampoco (no sobrescribe cuentas).
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final CrearAdministradorInicialUseCase useCase;
    private final String email;
    private final String password;

    public AdminBootstrapRunner(CrearAdministradorInicialUseCase useCase,
                                @Value("${app.bootstrap-admin.email:}") String email,
                                @Value("${app.bootstrap-admin.password:}") String password) {
        this.useCase = useCase;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        var resultado = useCase.crearSiNoExiste(new Command(email, password, "Administrador", "Inicial",
            "ADMIN-0001", "0000000000"));
        switch (resultado) {
            case CREADO -> log.info("ADMIN inicial creado: {}", email);
            case YA_EXISTE_ADMIN -> log.info("Ya existe un ADMIN; no se crea el ADMIN inicial");
            case EMAIL_EN_USO -> log.warn("No se creó el ADMIN inicial: el email {} ya pertenece a otra cuenta", email);
        }
    }
}
