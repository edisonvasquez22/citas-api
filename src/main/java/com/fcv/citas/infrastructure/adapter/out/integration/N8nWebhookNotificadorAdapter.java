package com.fcv.citas.infrastructure.adapter.out.integration;

import com.fcv.citas.application.port.out.ConsultaCitasIntegracionPort;
import com.fcv.citas.application.port.out.NotificadorCambioEstadoPort;
import com.fcv.citas.domain.model.CitaNotificable;
import com.fcv.citas.domain.model.EventoCambioEstado;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

/**
 * WF-002: envía el evento al webhook de n8n después del commit, para no notificar cambios
 * que luego se revierten. Sin N8N_WEBHOOK_URL configurado no hace nada.
 */
@Component
public class N8nWebhookNotificadorAdapter implements NotificadorCambioEstadoPort {

    private static final Logger log = LoggerFactory.getLogger(N8nWebhookNotificadorAdapter.class);
    static final String HEADER_SECRETO = "X-Webhook-Secret";

    private final ConsultaCitasIntegracionPort consultaPort;
    private final String webhookUrl;
    private final String webhookSecret;
    private final RestClient restClient;

    public N8nWebhookNotificadorAdapter(ConsultaCitasIntegracionPort consultaPort,
                                        @Value("${app.integration.webhook-url:}") String webhookUrl,
                                        @Value("${app.integration.webhook-secret:}") String webhookSecret) {
        this.consultaPort = consultaPort;
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public void notificar(EventoCambioEstado evento) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(evento);
                }
            });
        } else {
            enviar(evento);
        }
    }

    private void enviar(EventoCambioEstado evento) {
        try {
            Optional<CitaNotificable> cita = consultaPort.buscarPorId(evento.citaId());
            restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_SECRETO, webhookSecret)
                .body(payload(evento, cita.orElse(null)))
                .retrieve()
                .toBodilessEntity();
            log.info("Webhook n8n enviado: {} cita {}", evento.tipo(), evento.citaId());
        } catch (RuntimeException e) {
            log.warn("No se pudo notificar a n8n el evento {} de la cita {}: {}", evento.tipo(), evento.citaId(),
                e.getMessage());
        }
    }

    private static Map<String, Object> payload(EventoCambioEstado evento, CitaNotificable cita) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipo", evento.tipo().name());
        body.put("citaId", evento.citaId());
        body.put("solicitudReprogramacionId", evento.solicitudReprogramacionId());
        body.put("motivo", evento.motivo());
        body.put("momento", evento.momento().toString());
        if (cita != null) {
            body.put("estadoCita", cita.estado());
            body.put("inicio", cita.inicio().toString());
            body.put("fin", cita.fin().toString());
            body.put("pacienteNombre", cita.pacienteNombre());
            body.put("pacienteEmail", cita.pacienteEmail());
            body.put("profesionalNombre", cita.profesionalNombre());
            body.put("sedeCodigo", cita.sedeCodigo());
            body.put("sedeNombre", cita.sedeNombre());
            body.put("especialidadNombre", cita.especialidadNombre());
        }
        return body;
    }
}
