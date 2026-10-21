package dev.sogong_girls.mr_world.domain.booking.dto;

import dev.sogong_girls.mr_world.domain.booking.enums.Meal;
import dev.sogong_girls.mr_world.domain.tour.enums.AddonType;

public record BookingAddonResponse(
    Long id,
    Long addonOptionId,
    AddonType addonType,
    Integer day,
    Meal meal
) {}
