package dev.sogong_girls.mr_world.domain.booking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingCreateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingOptionUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingStatusUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.service.BookingService;
import jakarta.validation.Valid;

@RestController
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping("/api/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDetailResponse createBooking(@Valid @RequestBody BookingCreateRequest request) {
        return service.createBooking(request);
    }

    @GetMapping("/api/bookings")
    public List<BookingResponse> getBookingList() {
        return service.getBookingList();
    }

    @GetMapping("/api/bookings/{id}")
    public BookingDetailResponse getBooking(@PathVariable("id") Long id) {
        return service.getBooking(id);
    }

    @PatchMapping("/api/bookings/{id}/options")
    public BookingDetailResponse updateBookingOptions(@PathVariable("id") Long id,
            @Valid @RequestBody BookingOptionUpdateRequest request) {
        return service.updateBookingOptions(id, request);
    }

    @PatchMapping("/api/bookings/{id}/statuses")
    public BookingResponse updateBookingStatus(@PathVariable("id") Long id,
            @Valid @RequestBody BookingStatusUpdateRequest request) {
        return service.updateBookingStatus(id, request);
    }
}
