package dev.sogong_girls.mr_world.domain.tour.repository;

import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourRepository extends JpaRepository<Tour, Long> {
}
