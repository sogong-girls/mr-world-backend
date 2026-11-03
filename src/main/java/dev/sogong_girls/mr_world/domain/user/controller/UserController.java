package dev.sogong_girls.mr_world.domain.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import dev.sogong_girls.mr_world.global.security.JwtTokenProvider.JwtPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.user.dto.UserResponse;
import dev.sogong_girls.mr_world.domain.user.dto.UserUpdateRequest;
import dev.sogong_girls.mr_world.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(service.getUser(principal.userId()));
    }

    @PatchMapping("/api/users/me")
    public ResponseEntity<UserResponse> updateUser(@AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(service.updateUser(principal.userId(), request));
    }

    @GetMapping("/api/users/me/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingList(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(service.getBookingList(principal.userId()));
    }
}
