package dev.sogong_girls.mr_world.domain.tour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;

public interface TourStyleRepository extends JpaRepository<TourStyle, Long> {

    public List<TourStyle> findAllByTourId(Long tourId);

}
