package dev.sogong_girls.mr_world.domain.booking.dto;

import java.time.LocalDateTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.enums.Meal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingCreateRequest(
                @NotNull Long tourId,
                @NotNull Long tourStyleId,
                @NotNull Long hotelOptionId,
                @NotNull Long foodOptionId,
                @NotNull @Min(1) Integer participantsCount,
                @NotNull LocalDateTime startAt,
                @NotNull LocalDateTime endAt,
                @Valid List<BookingAddonRequest> bookingAddons) {

        public record BookingAddonRequest(
                        @NotNull Long addonOptionId,
                        @NotNull @Min(1) Integer nthDay,
                        @NotNull Meal meal) {
        }
}
