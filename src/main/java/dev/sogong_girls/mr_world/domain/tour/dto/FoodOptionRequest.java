package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.FoodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FoodOptionRequest(
    @NotBlank String name,
    @NotNull FoodType type,
    @NotNull Integer price,
    String description
) {}
