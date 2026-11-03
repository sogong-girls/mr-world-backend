package dev.sogong_girls.mr_world.domain.tour.dto;

import java.time.LocalTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.tour.entity.AddonOption;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;
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
        Integer nights,
        Integer days,
        LocalTime departureTime,
        LocalTime arrivalTime,
        String course,
        String description,
        List<TourStyle> styles,
        List<HotelOption> hotelOptions,
        List<FoodOption> foodOptions,
        List<AddonOption> addonOptions) {
}
