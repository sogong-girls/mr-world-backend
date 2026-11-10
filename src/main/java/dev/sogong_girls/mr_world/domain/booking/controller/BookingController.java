package dev.sogong_girls.mr_world.domain.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import dev.sogong_girls.mr_world.global.security.JwtTokenProvider.JwtPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingCreateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingOptionUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BookingController {
    private final BookingService service;

    @GetMapping("/api/bookings/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingById(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(service.getBookingById(id, principal.userId()));
    }

    @PostMapping("/api/bookings")
    public ResponseEntity<BookingDetailResponse> createBooking(@AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody BookingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBooking(principal.userId(), request));
    }

    @PatchMapping("/api/bookings/{id}/options")
    public ResponseEntity<BookingDetailResponse> updateBookingOptions(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody BookingOptionUpdateRequest request) {
        return ResponseEntity.ok(service.updateBookingOptions(id, principal.userId(), request));
    }

    @PostMapping("/api/bookings/{id}/payments")
    public ResponseEntity<BookingResponse> payBooking(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(service.payBooking(id, principal.userId()));
    }
}
