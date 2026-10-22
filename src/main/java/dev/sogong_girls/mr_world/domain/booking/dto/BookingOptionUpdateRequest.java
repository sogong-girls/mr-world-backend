package dev.sogong_girls.mr_world.domain.booking.dto;

import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.enums.Meal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

public record BookingOptionUpdateRequest(
        Long tourStyleId,
        Long hotelOptionId,
        Long foodOptionId,
        @Valid List<BookingAddonUpdateRequest> addons) {

    public record BookingAddonUpdateRequest(
            Long addonOptionId,
            @Min(1) Integer nthDay,
            Meal meal) {
    }
}
