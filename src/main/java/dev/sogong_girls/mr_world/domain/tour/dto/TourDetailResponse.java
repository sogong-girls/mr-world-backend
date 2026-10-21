package dev.sogong_girls.mr_world.domain.tour.dto;

import java.time.LocalTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;

public record TourDetailResponse(
                Long id,
                String name,
                String departure,
                String destination,
                TourTheme tourTheme,
                TransportType transportType,
                Long souvenirId,
                Integer nNights,
                Integer nDays,
                LocalTime departureTime,
                LocalTime arrivalTime,
                String course,
                String description,
                List<TourStyleResponse> styles,
                List<HotelOptionResponse> hotelOptions,
                List<FoodOptionResponse> foodOptions,
                List<AddonOptionResponse> addonOptions) {
}
