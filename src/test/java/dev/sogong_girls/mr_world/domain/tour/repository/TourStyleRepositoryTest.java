package dev.sogong_girls.mr_world.domain.tour.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TourStyleRepositoryTest {
    @Autowired
    TourStyleRepository tourStyleRepository;
    @Autowired
    EntityManager entityManager;

    @Test
    void findsLowestBasePriceAmongStylesForEachTour() {
        Tour busan = tour("Busan");
        Tour jeju = tour("Jeju");
        tour("NoStyle"); // 스타일이 없는 투어는 결과에 나오지 않아야 한다
        style(busan, TourStyleType.CLASSIC, 300000);
        style(busan, TourStyleType.GRAND, 200000);
        style(busan, TourStyleType.PREMIUM, 500000);
        style(jeju, TourStyleType.PREMIUM, 700000);
        entityManager.flush();
        entityManager.clear();

        assertThat(tourStyleRepository.findLowestPricesGroupByTourId())
                .extracting(TourLowestPrice::getTourId, TourLowestPrice::getLowestPrice)
                .containsExactlyInAnyOrder(
                        tuple(busan.getId(), 200000),
                        tuple(jeju.getId(), 700000));
    }

    private Tour tour(String name) {
        Tour tour = Tour.builder().name(name).departure("Seoul").destination(name)
                .tourTheme(TourTheme.values()[0]).transportType(TransportType.values()[0]).build();
        entityManager.persist(tour);
        return tour;
    }

    private void style(Tour tour, TourStyleType type, int basePrice) {
        entityManager.persist(TourStyle.builder().tourId(tour.getId()).type(type).basePrice(basePrice).build());
    }
}
