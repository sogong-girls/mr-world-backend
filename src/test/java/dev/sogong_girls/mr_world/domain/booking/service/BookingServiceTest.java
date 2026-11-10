package dev.sogong_girls.mr_world.domain.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingCreateRequest;
import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.booking.mapper.BookingMapper;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingAddonRepository;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingRepository;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceResponse;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.repository.AddonOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.FoodOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.HotelOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourStyleRepository;
import dev.sogong_girls.mr_world.domain.tour.service.TourService;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;
    @Mock
    BookingAddonRepository bookingAddonRepository;
    @Mock
    AddonOptionRepository addonOptionRepository;
    @Mock
    TourRepository tourRepository;
    @Mock
    TourStyleRepository tourStyleRepository;
    @Mock
    HotelOptionRepository hotelOptionRepository;
    @Mock
    FoodOptionRepository foodOptionRepository;
    @Mock
    TourService tourService;
    @Mock
    SmsNotificationService smsNotificationService;
    @Mock
    BookingMapper bookingMapper;
    @InjectMocks
    BookingService bookingService;

    @Test
    void returnsBookingDetailsForOwner() {
        Booking booking = Booking.builder().userId(10L).build();
        given(bookingRepository.findWithOptionsById(1L)).willReturn(Optional.of(booking));

        bookingService.getBookingById(1L, 10L);

        verify(bookingMapper).toDetailResponse(booking);
    }

    @Test
    void rejectsBookingDetailsForAnotherUser() {
        Booking booking = Booking.builder().userId(10L).build();
        given(bookingRepository.findWithOptionsById(1L)).willReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 20L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(BusinessErrorCode.UNAUTHORIZED));
        verifyNoInteractions(bookingMapper);
    }

    @Test
    void rejectsMissingBookingDetails() {
        given(bookingRepository.findWithOptionsById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 10L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(BusinessErrorCode.NOT_FOUND));
        verifyNoInteractions(bookingMapper);
    }

    @ParameterizedTest
    @ValueSource(ints = { 3, 4 })
    void confirmsLargeEnoughBookingWithoutLookingUpPendingBookings(int participants) {
        Booking booking = createBookingWithPending(participants, List.of());

        assertThat(booking.getStatus()).isEqualTo(TourStatus.CONFIRMED);
        assertThat(booking.getPaymentDueAt()).isNotNull();
        verifyNoMoreInteractions(bookingRepository);
    }

    @ParameterizedTest
    @CsvSource({ "1, 0", "2, 0", "1, 1" })
    void keepsBookingsPendingWhenTotalIsBelowThree(int participants, int existingParticipants) {
        List<Booking> pending = existingParticipants == 0 ? List.of() : List.of(pending(existingParticipants));

        Booking booking = createBookingWithPending(participants, pending);

        assertThat(booking.getStatus()).isEqualTo(TourStatus.PENDING);
        assertThat(booking.getPaymentDueAt()).isNull();
        assertThat(pending).allSatisfy(existing -> {
            assertThat(existing.getStatus()).isEqualTo(TourStatus.PENDING);
            assertThat(existing.getPaymentDueAt()).isNull();
        });
    }

    @ParameterizedTest
    @CsvSource({ "1, 2", "2, 1" })
    void confirmsNewAndExistingBookingWhenCombinedTotalReachesThree(int participants, int existingParticipants) {
        Booking existing = pending(existingParticipants);

        Booking booking = createBookingWithPending(participants, List.of(existing));

        assertThat(booking.getStatus()).isEqualTo(TourStatus.CONFIRMED);
        assertThat(existing.getStatus()).isEqualTo(TourStatus.CONFIRMED);
        assertThat(existing.getPaymentDueAt()).isNotNull();
    }

    @Test
    void confirmsBothPendingBookingsWhenThreeSingleParticipantsAreCombined() {
        Booking first = pending(1);
        Booking second = pending(1);

        Booking booking = createBookingWithPending(1, List.of(first, second));

        assertThat(List.of(booking, first, second)).allSatisfy(confirmed -> {
            assertThat(confirmed.getStatus()).isEqualTo(TourStatus.CONFIRMED);
            assertThat(confirmed.getPaymentDueAt()).isNotNull();
        });
    }

    @Test
    void leavesLaterBookingPendingOnceMinimumParticipantsAreReached() {
        Booking first = pending(2);
        Booking second = pending(1);

        Booking booking = createBookingWithPending(1, List.of(first, second));

        assertThat(booking.getStatus()).isEqualTo(TourStatus.CONFIRMED);
        assertThat(first.getStatus()).isEqualTo(TourStatus.CONFIRMED);
        assertThat(second.getStatus()).isEqualTo(TourStatus.PENDING);
        assertThat(second.getPaymentDueAt()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "2026-11-06T09:00, 2026-11-06T18:00, 0, 1",
            "2026-11-06T22:00, 2026-11-07T06:00, 1, 2",
            "2026-01-31T09:00, 2026-02-02T18:00, 2, 3",
            "2026-12-31T09:00, 2027-01-01T08:00, 1, 2",
            "2028-02-28T09:00, 2028-03-01T18:00, 2, 3"
    })
    void createsBookingWhenTimesAndCalendarDurationMatch(LocalDateTime startAt, LocalDateTime endAt,
            int nights, int days) {
        Tour tour = tour(nights, days, startAt.toLocalTime(), endAt.toLocalTime());
        stubBookingCreation(tour, 2);

        bookingService.createBooking(10L, request(startAt, endAt));

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking booking = captor.getValue();
        assertThat(booking.getStartAt()).isEqualTo(startAt);
        assertThat(booking.getEndAt()).isEqualTo(endAt);
        assertThat(booking.getNights()).isEqualTo(nights);
        assertThat(booking.getDays()).isEqualTo(days);
        verify(smsNotificationService).createNotifications(booking);
    }

    private void stubBookingCreation(Tour tour, int participants) {
        given(tourRepository.findById(1L)).willReturn(Optional.of(tour));
        TourStyle style = withId(TourStyle.builder().tourId(1L).type(TourStyleType.CLASSIC).build(), 2L);
        HotelOption hotel = withId(HotelOption.builder().tourId(1L).build(), 3L);
        FoodOption food = withId(FoodOption.builder().tourId(1L).build(), 4L);
        given(tourStyleRepository.findById(2L)).willReturn(Optional.of(style));
        given(hotelOptionRepository.findById(3L)).willReturn(Optional.of(hotel));
        given(foodOptionRepository.findById(4L)).willReturn(Optional.of(food));
        given(tourService.calculatePriceByIds(tour, style, hotel, food, List.of(), participants))
                .willReturn(new PriceResponse(200000, 0, 200000));
        given(bookingRepository.save(any(Booking.class))).willAnswer(invocation -> invocation.getArgument(0));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "2026-11-06T10:00, 2026-11-08T18:00, 2, 3",
            "2026-11-06T09:00, 2026-11-08T17:00, 2, 3",
            "2026-11-06T09:00:01, 2026-11-08T18:00, 2, 3",
            "2026-11-06T09:00, 2026-11-07T18:00, 2, 3",
            "2026-11-06T09:00, 2026-11-08T18:00, 1, 3",
            "2026-11-06T09:00, 2026-11-08T18:00, 2, 4",
            "2026-11-06T09:00, 2026-11-08T18:00, NULL, 3",
            "2026-11-06T09:00, 2026-11-08T18:00, 2, NULL",
            "NULL, 2026-11-08T18:00, 2, 3",
            "2026-11-06T09:00, NULL, 2, 3"
    }, nullValues = "NULL")
    void rejectsMismatchedOrMissingBookingDatesBeforeSaving(LocalDateTime startAt, LocalDateTime endAt,
            Integer nights, Integer days) {
        Tour tour = tour(nights, days, LocalTime.of(9, 0), LocalTime.of(18, 0));
        assertInvalidBookingDate(tour, startAt, endAt);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-11-06T18:00, 2026-11-06T09:00, 0, 1",
            "2026-11-06T09:00, 2026-11-06T09:00, 0, 1",
            "2026-11-08T09:00, 2026-11-06T18:00, 2, 3"
    })
    void rejectsEndNotAfterStartEvenWhenTimesMatch(LocalDateTime startAt, LocalDateTime endAt,
            int nights, int days) {
        Tour tour = tour(nights, days, startAt.toLocalTime(), endAt.toLocalTime());
        assertInvalidBookingDate(tour, startAt, endAt);
    }

    private void assertInvalidBookingDate(Tour tour, LocalDateTime startAt, LocalDateTime endAt) {
        given(tourRepository.findById(1L)).willReturn(Optional.of(tour));

        assertThatThrownBy(() -> bookingService.createBooking(10L, request(startAt, endAt)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(BusinessErrorCode.INVALID_INPUT));
        verifyNoInteractions(bookingRepository, bookingAddonRepository, tourService, smsNotificationService);
    }

    private Booking createBookingWithPending(int participants, List<Booking> pendingBookings) {
        LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 9, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 12, 2, 18, 0);
        stubBookingCreation(tour(1, 2, startAt.toLocalTime(), endAt.toLocalTime()), participants);
        if (participants < 3) {
            given(bookingRepository.findAllByTourIdAndStartAtAndEndAtAndStatusOrderByCreatedAtAscIdAsc(
                    1L, startAt, endAt, TourStatus.PENDING, PageRequest.of(0, 3 - participants)))
                    .willReturn(pendingBookings);
        }

        bookingService.createBooking(10L,
                new BookingCreateRequest(1L, 2L, 3L, 4L, participants, startAt, endAt, List.of()));

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        return captor.getValue();
    }

    private Booking pending(int participants) {
        return Booking.builder().participantsCount(participants).status(TourStatus.PENDING).build();
    }

    private Tour tour(Integer nights, Integer days, LocalTime departureTime, LocalTime arrivalTime) {
        return withId(Tour.builder().nights(nights).days(days)
                .departureTime(departureTime).arrivalTime(arrivalTime).build(), 1L);
    }

    private BookingCreateRequest request(LocalDateTime startAt, LocalDateTime endAt) {
        return new BookingCreateRequest(1L, 2L, 3L, 4L, 2, startAt, endAt, List.of());
    }

    private <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
