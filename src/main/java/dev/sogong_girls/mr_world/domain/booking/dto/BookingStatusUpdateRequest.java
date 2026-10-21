package dev.sogong_girls.mr_world.domain.booking.dto;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import jakarta.validation.constraints.NotNull;

public record BookingStatusUpdateRequest(
    @NotNull TourStatus status
) {}
