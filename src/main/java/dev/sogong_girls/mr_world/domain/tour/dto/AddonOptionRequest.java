package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.AddonType;
import jakarta.validation.constraints.NotNull;

public record AddonOptionRequest(
    @NotNull AddonType type,
    @NotNull Integer price,
    String description
) {}
