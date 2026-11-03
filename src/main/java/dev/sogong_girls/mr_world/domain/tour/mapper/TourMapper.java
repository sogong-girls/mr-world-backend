package dev.sogong_girls.mr_world.domain.tour.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TourMapper {

    TourResponse toResponse(Tour tour);

    List<TourResponse> toResponses(List<Tour> tours);

}
