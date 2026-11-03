package dev.sogong_girls.mr_world.domain.booking.dto;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import java.time.LocalDateTime;

public record BookingResponse(
    Long id,
    Long tourId,
    Long userId,
    TourStyleType tourStyleType,
    Integer totalPrice,
    Integer participantsCount,
    LocalDateTime startAt,
    LocalDateTime endAt,
    TourStatus status
) {}
