package dev.sogong_girls.mr_world.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.Test;

import dev.sogong_girls.mr_world.domain.user.enums.Role;
import dev.sogong_girls.mr_world.global.security.JwtTokenProvider.JwtTokenPair;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtTokenProviderTest {
    private static final String SECRET = "test-only-secret-key-with-at-least-32-bytes";
    private static final Instant NOW = Instant.parse("2026-11-03T00:00:00Z");
    private final JwtProperties properties = new JwtProperties(SECRET, Duration.ofMinutes(30), Duration.ofDays(14), false);
    private final JwtTokenProvider provider = providerAt(NOW);

    @Test
    void accessTokenExpiresAfterThirtyMinutes() {
        String token = provider.createTokens(1L, Role.USER).accessToken();
        assertThat(providerAt(NOW.plusSeconds(1799)).parseAccessToken(token).userId()).isEqualTo(1L);
        assertThatThrownBy(() -> providerAt(NOW.plusSeconds(1801)).parseAccessToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void refreshTokenExpiresAfterFourteenDays() {
        String token = provider.createTokens(1L, Role.USER).refreshToken();
        assertThat(providerAt(NOW.plus(Duration.ofDays(14)).minusSeconds(1)).parseRefreshToken(token)).isEqualTo(1L);
        assertThatThrownBy(() -> providerAt(NOW.plus(Duration.ofDays(14)).plusSeconds(1)).parseRefreshToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tokensCannotBeSubstitutedForEachOther() {
        JwtTokenPair tokens = provider.createTokens(1L, Role.ADMIN);
        assertThat(provider.parseAccessToken(tokens.accessToken()).role()).isEqualTo(Role.ADMIN);
        assertThatThrownBy(() -> provider.parseAccessToken(tokens.refreshToken())).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> provider.parseRefreshToken(tokens.accessToken())).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsWrongSignatureAndMissingExpiration() {
        var otherProperties = new JwtProperties("another-test-secret-key-with-at-least-32-bytes",
                Duration.ofMinutes(30), Duration.ofDays(14), false);
        String wrongSignature = new JwtTokenProvider(otherProperties, Clock.fixed(NOW, ZoneOffset.UTC))
                .createTokens(1L, Role.USER).accessToken();
        assertThatThrownBy(() -> provider.parseAccessToken(wrongSignature)).isInstanceOf(JwtException.class);

        String missingExpiration = Jwts.builder().issuer("mr-world").subject("1")
                .claim("tokenType", "access").claim("role", "USER").issuedAt(Date.from(NOW))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        assertThatThrownBy(() -> provider.parseAccessToken(missingExpiration)).isInstanceOf(JwtException.class);
    }

    private JwtTokenProvider providerAt(Instant instant) {
        return new JwtTokenProvider(properties, Clock.fixed(instant, ZoneOffset.UTC));
    }
}
