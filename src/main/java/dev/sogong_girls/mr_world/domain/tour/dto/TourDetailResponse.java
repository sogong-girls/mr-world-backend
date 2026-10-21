package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
import java.util.List;

public record TourDetailResponse(
    Long id,
    String name,
    String departure,
    String destination,
    TourTheme tourTheme,
    TransportType transportType,
    Long souvenirId,
    String description,
    List<TourStyleResponse> styles,
    List<HotelOptionResponse> hotelOptions,
    List<FoodOptionResponse> foodOptions,
    List<AddonOptionResponse> addonOptions
) {}
