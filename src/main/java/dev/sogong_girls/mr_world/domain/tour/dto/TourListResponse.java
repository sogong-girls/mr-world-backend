package dev.sogong_girls.mr_world.domain.tour.dto;

import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;

/**
 * 투어 목록 조회(GET /api/tours) 응답.
 *
 * @param lowestPrice 투어에 등록된 스타일(CLASSIC, GRAND, PREMIUM 등) 중 가장 낮은 기본가. 스타일이 없으면 null.
 * @param souvenir    투어에 연결된 기념품. 없으면 null.
 */
public record TourListResponse(
        Long id,
        String name,
        String departure,
        String destination,
        TourTheme tourTheme,
        Integer nights,
        Integer days,
        Integer lowestPrice,
        SouvenirResponse souvenir) {
}
