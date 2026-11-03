package dev.sogong_girls.mr_world.global.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secretKey,
        Duration accessTokenValidity,
        Duration refreshTokenValidity,
        boolean cookieSecure) {

    public JwtProperties {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("JWT_SECRET_KEY must be configured");
        }
        if (accessTokenValidity == null || accessTokenValidity.isNegative() || accessTokenValidity.isZero()
                || refreshTokenValidity == null || refreshTokenValidity.isNegative() || refreshTokenValidity.isZero()) {
            throw new IllegalArgumentException("JWT validity must be positive");
        }
    }
}
