package dev.sogong_girls.mr_world.domain.tour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;

public interface FoodOptionRepository extends JpaRepository<FoodOption, Long> {

    public List<FoodOption> findAllByTourId(Long tourId);

}
