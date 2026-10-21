package dev.sogong_girls.mr_world.domain.booking.dto;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import java.time.OffsetDateTime;

public record BookingResponse(
    Long id,
    Long tourId,
    Long userId,
    TourStyleType tourStyleType,
    Integer totalPrice,
    Integer participantsCount,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    TourStatus status
) {}
