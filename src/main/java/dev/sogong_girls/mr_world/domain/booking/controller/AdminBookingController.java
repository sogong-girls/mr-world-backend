package dev.sogong_girls.mr_world.domain.booking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingStatusUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.service.AdminBookingService;
import jakarta.validation.Valid;

@RestController
public class AdminBookingController {
    private final AdminBookingService service;

    public AdminBookingController(AdminBookingService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/bookings")
    public List<BookingResponse> getAllBookingList() {
        return service.getAllBookingList();
    }

    @GetMapping("/api/admin/bookings/{id}")
    public BookingDetailResponse getAdminBooking(@PathVariable("id") Long id) {
        return service.getAdminBooking(id);
    }

    @PatchMapping("/api/admin/bookings/{id}/status")
    public BookingResponse updateAdminBookingStatus(@PathVariable("id") Long id,
            @Valid @RequestBody BookingStatusUpdateRequest request) {
        return service.updateAdminBookingStatus(id, request);
    }
}
