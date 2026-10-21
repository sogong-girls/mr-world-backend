package dev.sogong_girls.mr_world.domain.tour.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import dev.sogong_girls.mr_world.domain.tour.dto.AddonOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.FoodOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.HotelOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourStyleResponse;
import dev.sogong_girls.mr_world.domain.tour.entity.AddonOption;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TourMapper {
    TourResponse toResponse(Tour tour);

    List<TourResponse> toResponses(List<Tour> tours);

    TourStyleResponse toResponse(TourStyle tourStyle);

    List<TourStyleResponse> toTourStyleResponses(List<TourStyle> tourStyles);

    HotelOptionResponse toResponse(HotelOption hotelOption);

    List<HotelOptionResponse> toHotelOptionResponses(List<HotelOption> hotelOptions);

    FoodOptionResponse toResponse(FoodOption foodOption);

    List<FoodOptionResponse> toFoodOptionResponses(List<FoodOption> foodOptions);

    AddonOptionResponse toResponse(AddonOption addonOption);

    List<AddonOptionResponse> toAddonOptionResponses(List<AddonOption> addonOptions);
}
