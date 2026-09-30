package com.fcv.citas.infrastructure.adapter.out.integration;

import com.fcv.citas.application.port.out.ConsultaCitasIntegracionPort;
import com.fcv.citas.domain.model.CitaNotificable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ConsultaCitasIntegracionJdbcAdapter implements ConsultaCitasIntegracionPort {

    private static final String SELECT_BASE = """
        SELECT a.id, st.code AS estado, a.scheduled_start_at, a.scheduled_end_at,
               pu.id AS paciente_id, CONCAT(pu.first_name, ' ', pu.last_name) AS paciente_nombre,
               pu.email AS paciente_email,
               CONCAT(prof_u.first_name, ' ', prof_u.last_name) AS profesional_nombre,
               l.code AS sede_codigo, l.name AS sede_nombre, sp.name AS especialidad_nombre
        FROM appointments a
        JOIN appointment_statuses st ON st.id = a.status_id
        JOIN users pu ON pu.id = a.patient_user_id
        JOIN professionals p ON p.id = a.professional_id
        JOIN users prof_u ON prof_u.id = p.user_id
        JOIN locations l ON l.id = a.location_id
        JOIN specialties sp ON sp.id = a.specialty_id
        """;

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultaCitasIntegracionJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CitaNotificable> listar(String estado, LocalDateTime desde, LocalDateTime hasta) {
        String sql = SELECT_BASE
            + " WHERE a.scheduled_start_at >= :desde AND a.scheduled_start_at < :hasta"
            + (estado == null ? "" : " AND st.code = :estado")
            + " ORDER BY a.scheduled_start_at";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("desde", desde)
            .addValue("hasta", hasta)
            .addValue("estado", estado);
        return jdbc.query(sql, params, (rs, i) -> mapear(rs));
    }

    @Override
    public Optional<CitaNotificable> buscarPorId(Long citaId) {
        return jdbc.query(SELECT_BASE + " WHERE a.id = :id", new MapSqlParameterSource("id", citaId),
            (rs, i) -> mapear(rs)).stream().findFirst();
    }

    private static CitaNotificable mapear(ResultSet rs) throws SQLException {
        return new CitaNotificable(
            rs.getLong("id"),
            rs.getString("estado"),
            rs.getObject("scheduled_start_at", LocalDateTime.class),
            rs.getObject("scheduled_end_at", LocalDateTime.class),
            rs.getLong("paciente_id"),
            rs.getString("paciente_nombre"),
            rs.getString("paciente_email"),
            rs.getString("profesional_nombre"),
            rs.getString("sede_codigo"),
            rs.getString("sede_nombre"),
            rs.getString("especialidad_nombre"));
    }
}
