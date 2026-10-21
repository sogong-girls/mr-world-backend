package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TourCreateRequest(
    @NotBlank String name,
    @NotBlank String departure,
    @NotBlank String destination,
    @NotNull TourTheme tourTheme,
    TransportType transportType,
    Long souvenirId,
    String description
) {}
