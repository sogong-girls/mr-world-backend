package dev.sogong_girls.mr_world.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.user.dto.LoginRequest;
import dev.sogong_girls.mr_world.domain.user.dto.LoginResponse;
import dev.sogong_girls.mr_world.domain.user.dto.SignupRequest;
import dev.sogong_girls.mr_world.domain.user.dto.UserResponse;
import dev.sogong_girls.mr_world.domain.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/api/auth/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.signup(request));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout() {
        service.logout();
        return ResponseEntity.ok().build();
    }
}
