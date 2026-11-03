package dev.sogong_girls.mr_world.domain.tour.dto;

import java.time.LocalTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.tour.enums.AddonType;
import dev.sogong_girls.mr_world.domain.tour.enums.FoodType;
import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TourCreateRequest(
        @NotBlank String name,
        @NotBlank String departure,
        @NotBlank String destination,
        @NotNull TourTheme tourTheme,
        TransportType transportType,
        Long souvenirId,
        Integer nights,
        Integer days,
        LocalTime departureTime,
        LocalTime arrivalTime,
        String course,
        String description,
        @Valid List<TourStyleRequest> tourStyles,
        @Valid List<AddonOptionRequest> addonOptions) {

    public record TourStyleRequest(
            @NotNull TourStyleType type,
            @NotNull Integer basePrice,
            Double regularDiscount,
            @Valid HotelOptionRequest hotelOption,
            @Valid FoodOptionRequest foodOption,
            String description) {
    }

    public record HotelOptionRequest(
            @NotBlank String name,
            @NotBlank String location,
            @NotNull HotelGrade grade,
            @NotNull Integer price,
            String description) {
    }

    public record FoodOptionRequest(
            @NotBlank String name,
            @NotNull FoodType type,
            @NotNull Integer price,
            String description) {
    }

    public record AddonOptionRequest(
            @NotNull AddonType type,
            @NotNull Integer price,
            String description) {
    }
}
