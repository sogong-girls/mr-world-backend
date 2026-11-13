package dev.sogong_girls.mr_world.domain.souvenir.dto;

import java.time.LocalDateTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;

public record SouvenirStockResponse(
        Long id,
        String name,
        String brand,
        String description,
        Integer stockCount,
        Integer requiredCount,
        List<BookingSouvenirRequirement> bookings) {

    public record BookingSouvenirRequirement(
            Long bookingId,
            Long tourId,
            LocalDateTime startAt,
            TourStatus status,
            Integer quantity) {
    }
}
