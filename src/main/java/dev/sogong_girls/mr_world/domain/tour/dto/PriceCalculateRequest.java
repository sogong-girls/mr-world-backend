package dev.sogong_girls.mr_world.domain.tour.dto;

import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.enums.Meal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PriceCalculateRequest(
        @NotNull Long tourStyleId,
        @NotNull Long hotelOptionId,
        @NotNull Long foodOptionId,
        @NotNull @Min(1) Integer participantsCount,
        @Valid List<AddonRequest> addons) {

    public record AddonRequest(
            Long addonOptionId,
            @Min(1) Integer nthDay,
            Meal meal) {
    }
}
