package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.FoodType;

public record FoodOptionResponse(
    Long id,
    Long tourId,
    String name,
    FoodType type,
    Integer price,
    String description
) {}
