package dev.sogong_girls.mr_world.domain.tour.repository;

import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodOptionRepository extends JpaRepository<FoodOption, Long> {
}
