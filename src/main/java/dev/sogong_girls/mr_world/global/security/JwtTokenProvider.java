package dev.sogong_girls.mr_world.global.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import dev.sogong_girls.mr_world.domain.user.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
    public record JwtTokenPair(String accessToken, String refreshToken) {
    }

    public record JwtPrincipal(Long userId, Role role) {
    }

    private static final String ISSUER = "mr-world";

    private final JwtProperties properties;
    private final Clock clock;
    private final SecretKey key;
    private final JwtParser accessParser;
    private final JwtParser refreshParser;

    @Autowired
    public JwtTokenProvider(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    JwtTokenProvider(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.key = Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8));
        this.accessParser = parser("access");
        this.refreshParser = parser("refresh");
    }

    public JwtTokenPair createTokens(Long userId, Role role) {
        return new JwtTokenPair(
                createToken(userId, role, "access", properties.accessTokenValidity()),
                createToken(userId, role, "refresh", properties.refreshTokenValidity()));
    }

    public JwtPrincipal parseAccessToken(String token) {
        Claims claims = accessParser.parseSignedClaims(token).getPayload();
        if (claims.get("role", String.class) == null) {
            throw new JwtException("Missing JWT role");
        }
        return new JwtPrincipal(userId(claims), Role.valueOf(claims.get("role", String.class)));
    }

    public Long parseRefreshToken(String token) {
        return userId(refreshParser.parseSignedClaims(token).getPayload());
    }

    private JwtParser parser(String tokenType) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .require("tokenType", tokenType)
                .clock(() -> Date.from(clock.instant()))
                .build();
    }

    private String createToken(Long userId, Role role, String tokenType, Duration validity) {
        Instant now = clock.instant();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .claim("role", role.name())
                .claim("tokenType", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(validity)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private Long userId(Claims claims) {
        if (claims.getExpiration() == null || claims.getSubject() == null) {
            throw new JwtException("Missing required JWT claims");
        }
        long userId = Long.parseLong(claims.getSubject());
        if (userId <= 0) {
            throw new JwtException("Invalid JWT subject");
        }
        return userId;
    }
}
