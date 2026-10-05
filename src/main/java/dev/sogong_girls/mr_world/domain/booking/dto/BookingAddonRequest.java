package dev.sogong_girls.mr_world.domain.booking.dto;

import dev.sogong_girls.mr_world.domain.booking.enums.Meal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingAddonRequest(
    @NotNull Long addonOptionId,
    @NotNull @Min(1) Integer day,
    @NotNull Meal meal
) {}
