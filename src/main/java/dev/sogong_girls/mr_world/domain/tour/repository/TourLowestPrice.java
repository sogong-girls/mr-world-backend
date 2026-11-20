package dev.sogong_girls.mr_world.domain.tour.repository;

/**
 * 투어별 최저 스타일 기본가 조회용 프로젝션.
 */
public interface TourLowestPrice {
    Long getTourId();

    Integer getLowestPrice();
}
