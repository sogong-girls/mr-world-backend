package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;

public record HotelOptionResponse(
    Long id,
    Long tourId,
    String name,
    String location,
    HotelGrade grade,
    Integer price,
    String description
) {}
