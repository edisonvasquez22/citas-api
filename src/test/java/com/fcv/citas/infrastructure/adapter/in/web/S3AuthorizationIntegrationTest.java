package com.fcv.citas.infrastructure.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fcv.citas.application.port.out.TokenProviderPort;
import com.fcv.citas.domain.model.RolNombre;
import com.fcv.citas.domain.model.Usuario;
import com.fcv.citas.testsupport.InMemoryPersistenceTestConfig;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de autorización básicas de S3 (GUIA_SESIONES_S2_S6.md, "Verificación
 * obligatoria" ítem 6): los endpoints /api/admin/** son exclusivos de ADMIN y
 * /api/professionals/me/** exclusivo de PROFESSIONAL, sin importar si el
 * recurso de negocio detrás existe o no.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(InMemoryPersistenceTestConfig.class)
class S3AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenProviderPort tokenProvider;

    private String tokenPara(RolNombre rol, String usuarioId) {
        Usuario usuario = Usuario.reconstruir(usuarioId, "Nombre", "Apellido", "CC", "0",
            "usuario" + usuarioId + "@example.com", "3000000000", "hash", EnumSet.of(rol), true);
        return tokenProvider.generarAccessToken(usuario);
    }

    @Test
    void adminSpecialties_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/admin/specialties")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminSpecialties_conTokenUser_devuelve403() throws Exception {
        mockMvc.perform(get("/api/admin/specialties")
                .header("Authorization", "Bearer " + tokenPara(RolNombre.USER, "1")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminSpecialties_conTokenAdmin_devuelve200() throws Exception {
        mockMvc.perform(get("/api/admin/specialties")
                .header("Authorization", "Bearer " + tokenPara(RolNombre.ADMIN, "2")))
            .andExpect(status().isOk());
    }

    @Test
    void availabilityBlocks_conTokenUser_devuelve403() throws Exception {
        mockMvc.perform(get("/api/professionals/me/availability-blocks")
                .header("Authorization", "Bearer " + tokenPara(RolNombre.USER, "3")))
            .andExpect(status().isForbidden());
    }

    @Test
    void availabilityBlocks_conTokenProfessional_pasaLaAutorizacion() throws Exception {
        // Pasa el filtro de rol (no 401/403); el 404 de negocio confirma que llegó
        // al caso de uso (no hay Profesional creado para este usuario en la prueba).
        mockMvc.perform(post("/api/professionals/me/availability-blocks")
                .header("Authorization", "Bearer " + tokenPara(RolNombre.PROFESSIONAL, "4"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sedeId\":1,\"fecha\":\"2027-01-01\",\"horaInicio\":\"08:00:00\",\"horaFin\":\"09:00:00\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void appointments_requiereAutenticacion() throws Exception {
        mockMvc.perform(get("/api/availability?especialidadId=1&fecha=2027-01-01"))
            .andExpect(status().isUnauthorized());
    }
}
