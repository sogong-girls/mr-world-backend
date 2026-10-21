package dev.sogong_girls.mr_world.domain.tour.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SouvenirCreateRequest(
    @NotBlank String name,
    String brand,
    String description,
    @NotNull @Min(0) Integer stockCount
) {}
