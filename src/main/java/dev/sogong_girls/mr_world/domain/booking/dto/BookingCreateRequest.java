package dev.sogong_girls.mr_world.domain.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public record BookingCreateRequest(
    @NotNull Long tourId,
    @NotNull Long tourStyleId,
    @NotNull Long hotelOptionId,
    @NotNull Long foodOptionId,
    @NotNull @Min(1) Integer participantsCount,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @Valid List<BookingAddonRequest> addons
) {}
