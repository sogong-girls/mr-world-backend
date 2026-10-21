package dev.sogong_girls.mr_world.domain.tour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;

public interface HotelOptionRepository extends JpaRepository<HotelOption, Long> {

    public List<HotelOption> findAllByTourId(Long tourId);

}
