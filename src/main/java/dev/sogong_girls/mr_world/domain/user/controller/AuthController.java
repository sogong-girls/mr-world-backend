package dev.sogong_girls.mr_world.domain.user.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.user.dto.LoginRequest;
import dev.sogong_girls.mr_world.domain.user.dto.SignupRequest;
import dev.sogong_girls.mr_world.domain.user.dto.UserResponse;
import dev.sogong_girls.mr_world.domain.user.service.AuthService;
import dev.sogong_girls.mr_world.global.security.JwtProperties;
import dev.sogong_girls.mr_world.global.security.JwtTokenProvider.JwtTokenPair;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    private final JwtProperties properties;

    @PostMapping("/api/auth/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.signup(request));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        setTokens(response, service.login(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        try {
            setTokens(response, service.refresh(refreshToken));
        } catch (BusinessException e) {
            clearTokens(response);
            throw e;
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        service.logout(refreshToken);
        clearTokens(response);
        return ResponseEntity.noContent().build();
    }

    private void setTokens(HttpServletResponse response, JwtTokenPair tokens) {
        addCookie(response, "access_token", tokens.accessToken(), "/", properties.accessTokenValidity());
        addCookie(response, "refresh_token", tokens.refreshToken(), "/api/auth", properties.refreshTokenValidity());
    }

    private void clearTokens(HttpServletResponse response) {
        addCookie(response, "access_token", "", "/", Duration.ZERO);
        addCookie(response, "refresh_token", "", "/api/auth", Duration.ZERO);
    }

    private void addCookie(HttpServletResponse response, String name, String value, String path, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
