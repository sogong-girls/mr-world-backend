package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import jakarta.validation.constraints.NotNull;

public record TourStyleRequest(
    @NotNull TourStyleType type,
    @NotNull Integer basePrice,
    Double regularDiscount,
    Long hotelOptionId,
    Long foodOptionId,
    String description
) {}
