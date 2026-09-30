package com.fcv.citas.infrastructure.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fcv.citas.testsupport.InMemoryPersistenceTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(InMemoryPersistenceTestConfig.class)
class IntegrationApiKeyIntegrationTest {

    private static final String RUTA = "/api/integration/appointments/daily-summary?fecha=2026-10-05";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sinApiKeyResponde401() throws Exception {
        mockMvc.perform(get(RUTA)).andExpect(status().isUnauthorized());
    }

    @Test
    void conApiKeyIncorrectaResponde401() throws Exception {
        mockMvc.perform(get(RUTA).header("X-Integration-Key", "otra-clave"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void conApiKeyCorrectaResponde200() throws Exception {
        mockMvc.perform(get(RUTA).header("X-Integration-Key", "test-integration-key"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void apiKeyNoDaAccesoAEndpointsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/appointments/requested").header("X-Integration-Key", "test-integration-key"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void recordatoriosConVentanaInvalidaResponde400() throws Exception {
        mockMvc.perform(get("/api/integration/appointments/reminders")
                .param("desde", "2026-10-05T10:00:00").param("hasta", "2026-10-05T09:00:00")
                .header("X-Integration-Key", "test-integration-key"))
            .andExpect(status().isBadRequest());
    }
}
