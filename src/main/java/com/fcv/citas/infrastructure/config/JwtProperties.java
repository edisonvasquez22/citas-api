package com.fcv.citas.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Enlaza {@code app.jwt.*} (application.yml) a JWT_ACCESS_SECRET/JWT_REFRESH_SECRET/etc. del .env. */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String accessSecret;
    private String refreshSecret;
    private long accessMinutes;
    private long refreshDays;

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public String getRefreshSecret() {
        return refreshSecret;
    }

    public void setRefreshSecret(String refreshSecret) {
        this.refreshSecret = refreshSecret;
    }

    public long getAccessMinutes() {
        return accessMinutes;
    }

    public void setAccessMinutes(long accessMinutes) {
        this.accessMinutes = accessMinutes;
    }

    public long getRefreshDays() {
        return refreshDays;
    }

    public void setRefreshDays(long refreshDays) {
        this.refreshDays = refreshDays;
    }
}
