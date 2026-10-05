package dev.sogong_girls.mr_world.domain.booking.dto;

import jakarta.validation.Valid;
import java.util.List;

public record BookingOptionUpdateRequest(
    Long tourStyleId,
    Long hotelOptionId,
    Long foodOptionId,
    @Valid List<BookingAddonRequest> addons
) {}
