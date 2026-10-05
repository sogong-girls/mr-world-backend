package dev.sogong_girls.mr_world.domain.booking.dto;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import java.time.OffsetDateTime;
import java.util.List;

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
    Integer participantsCount,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    Integer nNights,
    Integer nDays,
    TourStatus status,
    OffsetDateTime createdAt
) {}
