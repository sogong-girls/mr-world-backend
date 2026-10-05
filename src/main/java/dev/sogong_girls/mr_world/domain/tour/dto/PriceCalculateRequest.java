package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingAddonRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PriceCalculateRequest(
    @NotNull Long tourStyleId,
    @NotNull Long hotelOptionId,
    @NotNull Long foodOptionId,
    @NotNull @Min(1) Integer participantsCount,
    @Valid List<BookingAddonRequest> addons
) {}
