package dev.sogong_girls.mr_world.domain.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingCreateRequest;
import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
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
        given(tourRepository.findById(1L)).willReturn(Optional.of(tour));
        TourStyle style = withId(TourStyle.builder().tourId(1L).type(TourStyleType.CLASSIC).build(), 2L);
        HotelOption hotel = withId(HotelOption.builder().tourId(1L).build(), 3L);
        FoodOption food = withId(FoodOption.builder().tourId(1L).build(), 4L);
        given(tourStyleRepository.findById(2L)).willReturn(Optional.of(style));
        given(hotelOptionRepository.findById(3L)).willReturn(Optional.of(hotel));
        given(foodOptionRepository.findById(4L)).willReturn(Optional.of(food));
        given(tourService.calculatePriceByIds(tour, style, hotel, food, List.of(), 2))
                .willReturn(new PriceResponse(200000, 0, 200000));
        given(bookingRepository.save(any(Booking.class))).willAnswer(invocation -> invocation.getArgument(0));

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
