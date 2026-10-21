package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HotelOptionRequest(
    @NotBlank String name,
    @NotBlank String location,
    @NotNull HotelGrade grade,
    @NotNull Integer price,
    String description
) {}
