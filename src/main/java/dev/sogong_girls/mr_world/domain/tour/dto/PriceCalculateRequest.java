package dev.sogong_girls.mr_world.domain.tour.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PriceCalculateRequest(
        @NotNull Long tourStyleId,
        @NotNull Long hotelOptionId,
        @NotNull Long foodOptionId,
        @NotNull @Min(1) Integer participantsCount,
        @Valid List<Long> bookingAddonIds) {
}
