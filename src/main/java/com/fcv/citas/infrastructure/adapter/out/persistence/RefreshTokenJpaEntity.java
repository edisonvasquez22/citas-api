package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Mapea `refresh_tokens`. `id` es el `jti` del JWT (ver TokenProviderPort). */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected RefreshTokenJpaEntity() {
        // JPA
    }

    public RefreshTokenJpaEntity(String id, String userId, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
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
