package dev.sogong_girls.mr_world.domain.tour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;

public interface TourStyleRepository extends JpaRepository<TourStyle, Long> {

    public List<TourStyle> findAllByTourId(Long tourId);

    @Query("""
                SELECT s.tourId AS tourId, MIN(s.basePrice) AS lowestPrice
                FROM TourStyle s
                GROUP BY s.tourId
            """)
    List<TourLowestPrice> findLowestPricesGroupByTourId();

}
