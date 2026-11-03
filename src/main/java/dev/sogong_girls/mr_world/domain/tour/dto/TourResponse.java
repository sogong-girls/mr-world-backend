package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;

public record TourResponse(
                Long id,
                String name,
                String departure,
                String destination,
                TourTheme tourTheme,
                Integer nights,
                Integer days) {
}
