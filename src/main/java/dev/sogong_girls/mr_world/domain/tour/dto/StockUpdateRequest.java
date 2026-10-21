package dev.sogong_girls.mr_world.domain.tour.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
    @NotNull @Min(0) Integer stockCount
) {}
