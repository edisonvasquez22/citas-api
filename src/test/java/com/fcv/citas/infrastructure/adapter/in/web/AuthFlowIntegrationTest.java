package com.fcv.citas.infrastructure.adapter.in.web;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcv.citas.infrastructure.adapter.in.web.dto.AuthDtos;
import com.fcv.citas.testsupport.InMemoryPersistenceTestConfig;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Prueba end-to-end de HU-001/HU-002 sin base de datos real (ver
 * application-test.yml, que excluye datasource/JPA/Flyway, y
 * InMemoryPersistenceTestConfig, que sustituye los adaptadores JPA por dobles
 * en memoria): registro -> login -> refresh -> logout -> el refresh usado ya
 * no sirve. Es la evidencia de GOAL_01_GUIADO_SIMPLE.md.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(InMemoryPersistenceTestConfig.class)
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void flujoCompleto_registroLoginRefreshLogout() throws Exception {
        var registro = new AuthDtos.RegisterRequest(
            "Carlos", "Gómez", "CC", "1000000099",
            "carlos.gomez@example.com", "3000000099", "clave-segura-1"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.usuarioId", notNullValue()))
            .andExpect(jsonPath("$.email").value("carlos.gomez@example.com"));

        // Registrar el mismo email de nuevo debe fallar (CA-02 de HU-001).
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)))
            .andExpect(status().isConflict());

        var login = new AuthDtos.LoginRequest("carlos.gomez@example.com", "clave-segura-1");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andReturn();

        String loginBody = loginResult.getResponse().getContentAsString();
        String refreshToken = JsonPath.read(loginBody, "$.refreshToken");

        // Login con contraseña incorrecta debe fallar sin revelar el motivo (CA-02 de HU-002).
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AuthDtos.LoginRequest("carlos.gomez@example.com", "clave-incorrecta"))))
            .andExpect(status().isUnauthorized());

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDtos.RefreshRequest(refreshToken))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andReturn();

        // Rotación: el refresh ya usado queda revocado y no debe volver a servir (CA-04 de HU-002).
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDtos.RefreshRequest(refreshToken))))
            .andExpect(status().isUnauthorized());

        String refreshBody = refreshResult.getResponse().getContentAsString();
        String nuevoRefreshToken = JsonPath.read(refreshBody, "$.refreshToken");

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDtos.LogoutRequest(nuevoRefreshToken))))
            .andExpect(status().isNoContent());

        // Tras logout, ese refresh token ya no debe servir (CA-05 de HU-002).
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDtos.RefreshRequest(nuevoRefreshToken))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void registro_conDatosIncompletos_devuelve400ConDetalleDeCampos() throws Exception {
        var registroIncompleto = new AuthDtos.RegisterRequest(
            "", "Gómez", "CC", "1000000199", "no-es-un-email", "3000000199", "1234567"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroIncompleto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detalles", notNullValue()));
    }
}
