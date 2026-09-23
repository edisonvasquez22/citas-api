package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Mapea `refresh_tokens`. `id` es un autoincremental propio (surrogate key);
 * la fila se busca/revoca por {@code token_hash} (SHA-256 del `jti` del JWT,
 * ver RefreshTokenJpaAdapter), no por el id. `device_info` existe en el
 * esquema pero no se usa todavía; se omite el mapeo (Hibernate en modo
 * `validate` no exige mapear todas las columnas).
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected RefreshTokenJpaEntity() {
        // JPA
    }

    public RefreshTokenJpaEntity(Long userId, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void revocar() {
        this.revokedAt = Instant.now();
    }

    public boolean estaVigente() {
        return revokedAt == null && expiresAt.isAfter(Instant.now());
    }
}
