package dev.sogong_girls.mr_world.domain.booking.dto;

import java.time.LocalDateTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.dto.FoodOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.HotelOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;

public record BookingDetailResponse(
                Long id,
                Tour tour,
                Long userId,
                Long tourStyleId,
                TourStyleType tourStyleType,
                HotelOptionResponse hotelOption,
                FoodOptionResponse foodOption,
                List<BookingAddonResponse> bookingAddons,
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
