package dev.sogong_girls.mr_world.domain.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.enums.FoodType;
import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingRepositoryTest {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    EntityManager entityManager;

    @Test
    void findsOnlyPendingBookingsForSameTourAndScheduleOldestFirstWithLimit() {
        LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 9, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 12, 2, 18, 0);
        Tour tour = tour();
        HotelOption hotel = persist(HotelOption.builder().tourId(tour.getId()).name("Hotel").location("Busan")
                .grade(HotelGrade._3_STAR).price(100000).build());
        FoodOption food = persist(FoodOption.builder().tourId(tour.getId()).name("Food")
                .type(FoodType.LUNCHBOX).price(10000).build());

        Booking newest = booking(tour, hotel, food, startAt, endAt, TourStatus.PENDING);
        Booking oldest = booking(tour, hotel, food, startAt, endAt, TourStatus.PENDING);
        Booking middle = booking(tour, hotel, food, startAt, endAt, TourStatus.PENDING);
        ReflectionTestUtils.setField(oldest, "createdAt", startAt.minusDays(3));
        ReflectionTestUtils.setField(middle, "createdAt", startAt.minusDays(2));
        ReflectionTestUtils.setField(newest, "createdAt", startAt.minusDays(1));
        booking(tour(), hotel, food, startAt, endAt, TourStatus.PENDING);
        booking(tour, hotel, food, startAt.plusDays(1), endAt, TourStatus.PENDING);
        booking(tour, hotel, food, startAt, endAt.plusDays(1), TourStatus.PENDING);
        booking(tour, hotel, food, startAt, endAt, TourStatus.CONFIRMED);
        entityManager.flush();
        entityManager.clear();

        assertThat(bookingRepository.findAllByTourIdAndStartAtAndEndAtAndStatusOrderByCreatedAtAscIdAsc(
                tour.getId(), startAt, endAt, TourStatus.PENDING, PageRequest.of(0, 2)))
                .extracting(Booking::getId).containsExactly(oldest.getId(), middle.getId());
        assertThat(bookingRepository.findAllByTourIdAndStartAtAndEndAtAndStatusOrderByCreatedAtAscIdAsc(
                tour.getId(), startAt, endAt, TourStatus.PENDING, PageRequest.of(0, 10)))
                .extracting(Booking::getId).containsExactly(oldest.getId(), middle.getId(), newest.getId());
    }

    private Tour tour() {
        return persist(Tour.builder().name("Test tour").departure("Seoul").destination("Busan")
                .tourTheme(TourTheme.TREKKING).transportType(TransportType.PREMIUM_CAR).build());
    }

    private Booking booking(Tour tour, HotelOption hotel, FoodOption food, LocalDateTime startAt,
            LocalDateTime endAt, TourStatus status) {
        return persist(Booking.builder().tour(tour).userId(10L).tourStyleId(1L)
                .tourStyleType(TourStyleType.CLASSIC).hotelOption(hotel).foodOption(food)
                .totalPrice(100000).participantsCount(1).startAt(startAt).endAt(endAt).status(status).build());
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }
}
