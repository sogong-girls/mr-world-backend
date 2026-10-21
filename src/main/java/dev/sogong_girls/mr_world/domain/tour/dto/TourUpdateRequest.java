package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;

public record TourUpdateRequest(
    String name,
    String departure,
    String destination,
    TourTheme tourTheme,
    TransportType transportType,
    Long souvenirId,
    String description
) {}
