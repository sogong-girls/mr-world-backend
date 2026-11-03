package dev.sogong_girls.mr_world.domain.tour.dto;

import java.util.List;

import dev.sogong_girls.mr_world.domain.tour.enums.AddonType;
import dev.sogong_girls.mr_world.domain.tour.enums.FoodType;
import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
import jakarta.validation.Valid;

public record TourUpdateRequest(
        String name,
        String departure,
        String destination,
        TourTheme tourTheme,
        TransportType transportType,
        Long souvenirId,
        String description,
        @Valid List<TourStyleRequest> tourStyles,
        @Valid List<AddonOptionRequest> addonOptions) {

    public record TourStyleRequest(
            Long id,
            TourStyleType type,
            Integer basePrice,
            Double regularDiscount,
            @Valid HotelOptionRequest hotelOption,
            @Valid FoodOptionRequest foodOption,
            String description) {
    }

    public record HotelOptionRequest(
            Long id,
            String name,
            String location,
            HotelGrade grade,
            Integer price,
            String description) {
    }

    public record FoodOptionRequest(
            Long id,
            String name,
            FoodType type,
            Integer price,
            String description) {
    }

    public record AddonOptionRequest(
            Long id,
            AddonType type,
            Integer price,
            String description) {
    }
}
