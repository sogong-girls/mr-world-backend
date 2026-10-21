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
        return ResponseEntity.ok(service.getUser());
    }

    @PatchMapping("/api/users/me")
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(service.updateUser(request));
    }

    @GetMapping("/api/users/me/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingList() {
        return ResponseEntity.ok(service.getBookingList());
    }
}
