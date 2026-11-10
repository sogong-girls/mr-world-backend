package dev.sogong_girls.mr_world.domain.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingStatusUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.entity.BookingAddon;
import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;
import dev.sogong_girls.mr_world.domain.booking.enums.Meal;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.entity.AddonOption;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.enums.AddonType;
import dev.sogong_girls.mr_world.domain.tour.enums.FoodType;
import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminBookingServiceTest {
    @Autowired
    AdminBookingService service;
    @Autowired
    EntityManager entityManager;

    private Tour tour;
    private HotelOption hotel;
    private FoodOption food;

    @BeforeEach
    void setUp() {
        tour = persist(Tour.builder().name("Test tour").departure("Seoul").destination("Busan")
                .tourTheme(TourTheme.TREKKING).transportType(TransportType.PREMIUM_CAR).build());
        hotel = persist(HotelOption.builder().tourId(tour.getId()).name("Test hotel").location("Busan")
                .grade(HotelGrade._3_STAR).price(100000).build());
        food = persist(FoodOption.builder().tourId(tour.getId()).name("Test food")
                .type(FoodType.LUNCHBOX).price(10000).build());
    }

    @Test
    void listsBookingsForAllUsers() {
        Booking first = booking(10L, TourStatus.PENDING);
        Booking second = booking(20L, TourStatus.ARRIVED);
        flushAndClear();

        assertThat(service.getAllBookingList()).extracting(br -> br.id())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(service.getAllBookingList()).extracting(br -> br.userId())
                .containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void loadsBookingAndAllOptionsForUseAfterDetaching() {
        Booking booking = booking(10L, TourStatus.PENDING);
        AddonOption option = persist(AddonOption.builder().tourId(tour.getId()).type(AddonType.COFFEE)
                .price(5000).description("Coffee option").build());
        persist(BookingAddon.builder().bookingId(booking.getId()).addonOption(option)
                .nthDay(1).meal(Meal.BREAKFAST).build());
        flushAndClear();

        BookingDetailResponse response = service.getAdminBooking(booking.getId());
        entityManager.clear();

        assertThat(response.id()).isEqualTo(booking.getId());
        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.tour().getName()).isEqualTo("Test tour");
        assertThat(response.hotelOption().getName()).isEqualTo("Test hotel");
        assertThat(response.foodOption().getName()).isEqualTo("Test food");
        assertThat(response.bookingAddons()).singleElement().satisfies(addon -> {
            assertThat(addon.getNthDay()).isEqualTo(1);
            assertThat(addon.getAddonOption().getDescription()).isEqualTo("Coffee option");
        });
    }

    @Test
    void loadsBookingWithoutAddonOptions() {
        Booking booking = booking(10L, TourStatus.PENDING);
        flushAndClear();

        assertThat(service.getAdminBooking(booking.getId()).bookingAddons()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("statusTransitions")
    void allowsEveryStatusTransitionEvenForPaidBookings(TourStatus previous, TourStatus next) {
        Booking booking = booking(10L, TourStatus.PENDING);
        booking.updatePaidAt();
        booking.updateStatus(previous);
        flushAndClear();
        LocalDateTime paidAt = entityManager.find(Booking.class, booking.getId()).getPaidAt();

        BookingResponse response = service.updateAdminBookingStatus(booking.getId(),
                new BookingStatusUpdateRequest(next));
        flushAndClear();

        Booking saved = entityManager.find(Booking.class, booking.getId());
        assertThat(response.status()).isEqualTo(next);
        assertThat(saved.getStatus()).isEqualTo(next);
        assertThat(saved.getPaidAt()).isEqualTo(paidAt);
        if (next == TourStatus.CONFIRMED) {
            assertThat(saved.getPaymentDueAt()).isAfter(LocalDateTime.now());
        }
    }

    static Stream<Arguments> statusTransitions() {
        return Stream.of(TourStatus.values()).flatMap(previous -> Stream.of(TourStatus.values())
                .map(next -> Arguments.of(previous, next)));
    }

    @Test
    void reportsMissingBookingForDetailsAndStatusUpdates() {
        assertNotFound(() -> service.getAdminBooking(Long.MAX_VALUE));
        assertNotFound(() -> service.updateAdminBookingStatus(Long.MAX_VALUE,
                new BookingStatusUpdateRequest(TourStatus.CONFIRMED)));
    }

    @Test
    void listsAllMessagesRegardlessOfSentAtSendDueOrBookingStatus() {
        Booking booking = booking(10L, TourStatus.ARRIVED);
        SmsNotification overdue = notification(booking, LocalDateTime.now().minusDays(1));
        SmsNotification future = notification(booking, LocalDateTime.now().plusDays(1));
        SmsNotification unscheduled = notification(booking, null);
        SmsNotification sent = notification(booking, LocalDateTime.now().minusDays(1));
        sent.updateSentAt();
        flushAndClear();

        assertThat(service.getSmsNotificationList()).extracting(sr -> sr.id())
                .containsExactlyInAnyOrder(overdue.getId(), future.getId(), unscheduled.getId(), sent.getId());
        assertThat(service.getSmsNotificationList()).filteredOn(response -> response.id().equals(sent.getId()))
                .singleElement().satisfies(response -> assertThat(response.sentAt()).isNotNull());
    }

    @Test
    void returnsEmptyListsWhenNoBookingsOrMessagesExist() {
        assertThat(service.getAllBookingList()).isEmpty();
        assertThat(service.getSmsNotificationList()).isEmpty();
    }

    private Booking booking(Long userId, TourStatus status) {
        return persist(Booking.builder().tour(tour).userId(userId).tourStyleId(1L)
                .tourStyleType(TourStyleType.CLASSIC).hotelOption(hotel).foodOption(food)
                .totalPrice(200000).participantsCount(1).startAt(LocalDateTime.of(2026, 12, 1, 9, 0))
                .endAt(LocalDateTime.of(2026, 12, 2, 18, 0)).status(status).build());
    }

    private SmsNotification notification(Booking booking, LocalDateTime sendDue) {
        return persist(SmsNotification.builder().bookingId(booking.getId()).phoneNumber("01012345678")
                .sendDue(sendDue).message("Test notification").build());
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void assertNotFound(Runnable action) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(BusinessErrorCode.NOT_FOUND));
    }
}
