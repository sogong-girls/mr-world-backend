package dev.sogong_girls.mr_world.domain.booking.dto;

import java.time.LocalDateTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.entity.BookingAddon;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;

public record BookingDetailResponse(
        Long id,
        Tour tour,
        Long userId,
        Long tourStyleId,
        TourStyleType tourStyleType,
        HotelOption hotelOption,
        FoodOption foodOption,
        List<BookingAddon> bookingAddons,
        Integer totalPrice,
        Integer discountPrice,
        LocalDateTime paymentDueAt,
        LocalDateTime paidAt,
        Integer participantsCount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer nights,
        Integer days,
        TourStatus status,
        LocalDateTime createdAt) {
}
