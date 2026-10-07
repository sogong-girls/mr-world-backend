package dev.sogong_girls.mr_world.domain.booking.dto;

import java.time.OffsetDateTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;

public record BookingDetailResponse(
        Long id,
        Long tourId,
        Long userId,
        Long tourStyleId,
        TourStyleType tourStyleType,
        Long hotelOptionId,
        Long foodOptionId,
        List<BookingAddonResponse> addons,
        Integer totalPrice,
        Integer discountPrice,
        OffsetDateTime paymentDueAt,
        OffsetDateTime paidAt,
        Integer participantsCount,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Integer nNights,
        Integer nDays,
        TourStatus status,
        OffsetDateTime createdAt) {
}
