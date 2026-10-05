package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.AddonType;

public record AddonOptionResponse(
    Long id,
    Long tourId,
    AddonType type,
    Integer price,
    String description
) {}
