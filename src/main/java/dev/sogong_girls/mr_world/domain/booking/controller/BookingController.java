package dev.sogong_girls.mr_world.domain.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/api/bookings")
    public ResponseEntity<BookingDetailResponse> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBooking(request));
    }

    @PatchMapping("/api/bookings/{id}/options")
    public ResponseEntity<BookingDetailResponse> updateBookingOptions(@PathVariable("id") Long id,
            @Valid @RequestBody BookingOptionUpdateRequest request) {
        return ResponseEntity.ok(service.updateBookingOptions(id, request));
    }

    @PostMapping("/api/bookings/{id}/payments")
    public ResponseEntity<BookingResponse> payBooking(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.payBooking(id));
    }
}
