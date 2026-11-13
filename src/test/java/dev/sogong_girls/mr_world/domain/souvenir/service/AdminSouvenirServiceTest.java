package dev.sogong_girls.mr_world.domain.souvenir.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirStockResponse;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirStockResponse.BookingSouvenirRequirement;
import dev.sogong_girls.mr_world.domain.souvenir.entity.Souvenir;
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
class AdminSouvenirServiceTest {
    private static final LocalDateTime BASE_START = LocalDateTime.of(2026, 12, 1, 9, 0);

    @Autowired
    AdminSouvenirService service;
    @Autowired
    EntityManager entityManager;

    private Souvenir souvenir;
    private Tour tour;
    private HotelOption hotel;
    private FoodOption food;

    @BeforeEach
    void setUp() {
        souvenir = souvenir("Keyring", 50);
        tour = tour(souvenir.getId());
        hotel = persist(HotelOption.builder().tourId(tour.getId()).name("Test hotel").location("Busan")
                .grade(HotelGrade._3_STAR).price(100000).build());
        food = persist(FoodOption.builder().tourId(tour.getId()).name("Test food")
                .type(FoodType.LUNCHBOX).price(10000).build());
    }

    @Test
    void returnsStockAndRequiredQuantityPerBookingForEachSouvenir() {
        Souvenir other = souvenir("Postcard", 3);
        Tour sameSouvenirTour = tour(souvenir.getId());
        Tour otherSouvenirTour = tour(other.getId());

        Booking later = booking(tour, TourStatus.BOOKED, 4, BASE_START.plusDays(5));
        Booking earlier = booking(sameSouvenirTour, TourStatus.BOOKED, 2, BASE_START);
        Booking otherBooking = booking(otherSouvenirTour, TourStatus.BOOKED, 7, BASE_START);
        flushAndClear();

        List<SouvenirStockResponse> responses = service.getSouvenirList();

        assertThat(responses).extracting(SouvenirStockResponse::id)
                .containsExactlyInAnyOrder(souvenir.getId(), other.getId());

        assertThat(find(responses, souvenir.getId())).satisfies(response -> {
            assertThat(response.name()).isEqualTo("Keyring");
            assertThat(response.stockCount()).isEqualTo(50);
            assertThat(response.requiredCount()).isEqualTo(6);
            assertThat(response.bookings()).containsExactly(
                    new BookingSouvenirRequirement(earlier.getId(), sameSouvenirTour.getId(), BASE_START,
                            TourStatus.BOOKED, 2),
                    new BookingSouvenirRequirement(later.getId(), tour.getId(), BASE_START.plusDays(5),
                            TourStatus.BOOKED, 4));
        });

        // 재고보다 필요 수량이 많아도 그대로 반환한다
        assertThat(find(responses, other.getId())).satisfies(response -> {
            assertThat(response.stockCount()).isEqualTo(3);
            assertThat(response.requiredCount()).isEqualTo(7);
            assertThat(response.bookings()).extracting(BookingSouvenirRequirement::bookingId)
                    .containsExactly(otherBooking.getId());
        });
    }

    @Test
    void countsOnlyBookedBookings() {
        Booking booked = booking(tour, TourStatus.BOOKED, 3, BASE_START);
        booking(tour, TourStatus.CONFIRMED, 2, BASE_START);
        booking(tour, TourStatus.PENDING, 1, BASE_START);
        flushAndClear();

        SouvenirStockResponse response = find(service.getSouvenirList(), souvenir.getId());

        assertThat(response.requiredCount()).isEqualTo(3);
        assertThat(response.bookings()).singleElement().satisfies(requirement -> {
            assertThat(requirement.bookingId()).isEqualTo(booked.getId());
            assertThat(requirement.status()).isEqualTo(TourStatus.BOOKED);
        });
    }

    @ParameterizedTest
    @EnumSource(value = TourStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "BOOKED")
    void excludesBookingsNotInBookedStatus(TourStatus status) {
        booking(tour, status, 3, BASE_START);
        flushAndClear();

        SouvenirStockResponse response = find(service.getSouvenirList(), souvenir.getId());

        assertThat(response.requiredCount()).isZero();
        assertThat(response.bookings()).isEmpty();
    }

    @Test
    void ignoresBookingsForToursWithoutSouvenir() {
        Tour noSouvenirTour = tour(null);
        booking(noSouvenirTour, TourStatus.BOOKED, 5, BASE_START);
        flushAndClear();

        assertThat(service.getSouvenirList()).singleElement().satisfies(response -> {
            assertThat(response.id()).isEqualTo(souvenir.getId());
            assertThat(response.stockCount()).isEqualTo(50);
            assertThat(response.requiredCount()).isZero();
            assertThat(response.bookings()).isEmpty();
        });
    }

    private SouvenirStockResponse find(List<SouvenirStockResponse> responses, Long souvenirId) {
        return responses.stream()
                .filter(response -> response.id().equals(souvenirId))
                .findFirst()
                .orElseThrow();
    }

    private Souvenir souvenir(String name, int stockCount) {
        return persist(Souvenir.builder().name(name).description("Test souvenir")
                .stockCount(stockCount).build());
    }

    private Tour tour(Long souvenirId) {
        return persist(Tour.builder().name("Test tour").departure("Seoul").destination("Busan")
                .tourTheme(TourTheme.TREKKING).transportType(TransportType.PREMIUM_CAR)
                .souvenirId(souvenirId).build());
    }

    private Booking booking(Tour tour, TourStatus status, int participantsCount, LocalDateTime startAt) {
        return persist(Booking.builder().tour(tour).userId(10L).tourStyleId(1L)
                .tourStyleType(TourStyleType.CLASSIC).hotelOption(hotel).foodOption(food)
                .totalPrice(200000).participantsCount(participantsCount).startAt(startAt)
                .endAt(startAt.plusDays(1)).status(status).build());
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
