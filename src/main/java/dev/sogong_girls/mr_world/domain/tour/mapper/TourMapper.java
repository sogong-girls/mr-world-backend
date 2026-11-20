package dev.sogong_girls.mr_world.domain.tour.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourListResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TourMapper {

    TourResponse toResponse(Tour tour);

    List<TourResponse> toResponses(List<Tour> tours);

    // id, name은 SouvenirResponse에도 있어서 출처를 명시한다
    @Mapping(target = "id", source = "tour.id")
    @Mapping(target = "name", source = "tour.name")
    @Mapping(target = "lowestPrice", source = "lowestPrice")
    @Mapping(target = "souvenir", source = "souvenir")
    TourListResponse toListResponse(Tour tour, Integer lowestPrice, SouvenirResponse souvenir);

}
