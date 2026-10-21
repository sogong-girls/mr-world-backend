package dev.sogong_girls.mr_world.domain.tour.dto;

import java.time.LocalTime;

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
                Integer nNights,
                Integer nDays,
                LocalTime departureTime,
                LocalTime arrivalTime,
                String course,
                String description) {
}
