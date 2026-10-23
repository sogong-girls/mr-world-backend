package dev.sogong_girls.mr_world.domain.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserResponse> getUser() {
        // TODO: JWT 인증 연동 후 토큰에서 사용자 ID 구하기 (임시로 userId 0L 사용)
        return ResponseEntity.ok(service.getUser(0L));
    }

    @PatchMapping("/api/users/me")
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        // TODO: JWT 인증 연동 후 토큰에서 사용자 ID 구하기 (임시로 userId 0L 사용)
        return ResponseEntity.ok(service.updateUser(0L, request));
    }

    @GetMapping("/api/users/me/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingList() {
        // TODO: JWT 인증 연동 후 토큰에서 사용자 ID 구하기 (임시로 userId 0L 사용)
        return ResponseEntity.ok(service.getBookingList(0L));
    }
}
