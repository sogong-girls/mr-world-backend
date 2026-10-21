package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;

public record TourStyleResponse(
    Long id,
    Long tourId,
    TourStyleType type,
    Integer basePrice,
    Double regularDiscount,
    Long hotelOptionId,
    Long foodOptionId,
    String description
) {}
