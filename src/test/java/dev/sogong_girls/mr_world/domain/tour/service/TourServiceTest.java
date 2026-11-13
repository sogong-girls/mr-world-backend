package dev.sogong_girls.mr_world.domain.tour.service;

import static dev.sogong_girls.mr_world.domain.tour.enums.FoodType.LOCAL_RESTAURANT;
import static dev.sogong_girls.mr_world.domain.tour.enums.FoodType.LUNCHBOX;
import static dev.sogong_girls.mr_world.domain.tour.enums.FoodType.PREMIUM_RESTAURANT;
import static dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade._3_STAR;
import static dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade._4_STAR;
import static dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade._5_STAR;
import static dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType.GRAND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.souvenir.entity.Souvenir;
import dev.sogong_girls.mr_world.domain.souvenir.mapper.SouvenirMapper;
import dev.sogong_girls.mr_world.domain.souvenir.repository.SouvenirRepository;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceCalculateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourDetailResponse;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;
import dev.sogong_girls.mr_world.domain.tour.enums.FoodType;
import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.mapper.TourMapper;
import dev.sogong_girls.mr_world.domain.tour.repository.AddonOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.FoodOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.HotelOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourStyleRepository;
import dev.sogong_girls.mr_world.domain.user.entity.User;
import dev.sogong_girls.mr_world.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {
    @Mock
    TourRepository tourRepository;
    @Mock
    TourStyleRepository tourStyleRepository;
    @Mock
    HotelOptionRepository hotelOptionRepository;
    @Mock
    FoodOptionRepository foodOptionRepository;
    @Mock
    AddonOptionRepository addonOptionRepository;
    @Mock
    SouvenirRepository souvenirRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    TourMapper tourMapper;
    @Mock
    SouvenirMapper souvenirMapper;
    @InjectMocks
    TourService tourService;

    @Test
    void includesSouvenirInfoInTourDetail() {
        Souvenir souvenir = Souvenir.builder().name("Keyring").description("Desc")
                .stockCount(10).build();
        SouvenirResponse souvenirResponse = new SouvenirResponse(5L, "Keyring", "Desc", 10);
        given(tourRepository.findById(1L)).willReturn(Optional.of(Tour.builder().souvenirId(5L).build()));
        given(souvenirRepository.findById(5L)).willReturn(Optional.of(souvenir));
        given(souvenirMapper.toResponse(souvenir)).willReturn(souvenirResponse);

        TourDetailResponse result = tourService.getTour(1L);

        assertThat(result.souvenir()).isEqualTo(souvenirResponse);
    }

    @Test
    void returnsNullSouvenirWhenTourHasNoSouvenir() {
        given(tourRepository.findById(1L)).willReturn(Optional.of(Tour.builder().build()));

        TourDetailResponse result = tourService.getTour(1L);

        assertThat(result.souvenir()).isNull();
        then(souvenirRepository).shouldHaveNoInteractions();
    }

    @Test
    void returnsNullSouvenirWhenLinkedSouvenirIsMissing() {
        given(tourRepository.findById(1L)).willReturn(Optional.of(Tour.builder().souvenirId(5L).build()));
        given(souvenirRepository.findById(5L)).willReturn(Optional.empty());

        TourDetailResponse result = tourService.getTour(1L);

        assertThat(result.souvenir()).isNull();
    }

    @ParameterizedTest(name = "{0}, hotel={1}, food={2} => {3}")
    @MethodSource("gradeCases")
    void calculatesRequestedPriceAddingOnlyOptionsAboveStyleGrade(TourStyleType styleType,
            HotelGrade hotelGrade, FoodType foodType, int expectedTotal) {
        TourStyle style = style(styleType);
        HotelOption hotel = hotel(hotelGrade);
        FoodOption food = food(foodType);
        given(tourRepository.findById(1L)).willReturn(Optional.of(Tour.builder().build()));
        given(tourStyleRepository.findById(2L)).willReturn(Optional.of(style));
        given(hotelOptionRepository.findById(3L)).willReturn(Optional.of(hotel));
        given(foodOptionRepository.findById(4L)).willReturn(Optional.of(food));
        PriceCalculateRequest request = new PriceCalculateRequest(2L, 3L, 4L, 2, List.of());

        PriceResponse result = tourService.calculatePrice(1L, null, request);

        assertThat(result).isEqualTo(new PriceResponse(expectedTotal, 0, expectedTotal));
    }

    @ParameterizedTest(name = "{0}, hotel={1}, food={2} => {3}")
    @MethodSource("gradeCases")
    void calculatesBookingPriceAddingOnlyOptionsAboveStyleGrade(TourStyleType styleType,
            HotelGrade hotelGrade, FoodType foodType, int expectedTotal) {
        PriceResponse result = tourService.calculatePriceByIds(Tour.builder().build(),
                style(styleType), hotel(hotelGrade), food(foodType), List.of(), 2, null);

        assertThat(result).isEqualTo(new PriceResponse(expectedTotal, 0, expectedTotal));
    }

    // 단골 할인: 최종 금액 = 전체 금액 × (1 - regularDiscount)
    // style(GRAND) + _3_STAR + LUNCHBOX, 2명 => 전체 금액 200,000

    @Test
    void appliesRegularDiscountToRequestedPriceForRegularUser() {
        stubRequestedPriceOptions(discountedStyle(0.1f));
        given(userRepository.findById(10L)).willReturn(Optional.of(user(true)));

        PriceResponse result = tourService.calculatePrice(1L, 10L, priceRequest());

        assertThat(result).isEqualTo(new PriceResponse(200000, 20000, 180000));
    }

    @Test
    void doesNotApplyRegularDiscountToRequestedPriceForNonRegularUser() {
        stubRequestedPriceOptions(discountedStyle(0.1f));
        given(userRepository.findById(10L)).willReturn(Optional.of(user(false)));

        PriceResponse result = tourService.calculatePrice(1L, 10L, priceRequest());

        assertThat(result).isEqualTo(new PriceResponse(200000, 0, 200000));
    }

    @Test
    void doesNotApplyRegularDiscountToRequestedPriceForAnonymousUser() {
        stubRequestedPriceOptions(discountedStyle(0.1f));

        PriceResponse result = tourService.calculatePrice(1L, null, priceRequest());

        assertThat(result).isEqualTo(new PriceResponse(200000, 0, 200000));
        then(userRepository).shouldHaveNoInteractions();
    }

    @Test
    void appliesRegularDiscountToBookingPriceForRegularUser() {
        given(userRepository.findById(10L)).willReturn(Optional.of(user(true)));

        PriceResponse result = tourService.calculatePriceByIds(Tour.builder().build(),
                discountedStyle(0.15f), hotel(_3_STAR), food(LUNCHBOX), List.of(), 2, 10L);

        assertThat(result).isEqualTo(new PriceResponse(200000, 30000, 170000));
    }

    @Test
    void doesNotApplyRegularDiscountWhenUserDoesNotExist() {
        given(userRepository.findById(10L)).willReturn(Optional.empty());

        PriceResponse result = tourService.calculatePriceByIds(Tour.builder().build(),
                discountedStyle(0.1f), hotel(_3_STAR), food(LUNCHBOX), List.of(), 2, 10L);

        assertThat(result).isEqualTo(new PriceResponse(200000, 0, 200000));
    }

    @Test
    void skipsUserLookupWhenStyleHasNoRegularDiscount() {
        PriceResponse nullRate = tourService.calculatePriceByIds(Tour.builder().build(),
                discountedStyle(null), hotel(_3_STAR), food(LUNCHBOX), List.of(), 2, 10L);
        PriceResponse zeroRate = tourService.calculatePriceByIds(Tour.builder().build(),
                discountedStyle(0f), hotel(_3_STAR), food(LUNCHBOX), List.of(), 2, 10L);

        assertThat(nullRate).isEqualTo(new PriceResponse(200000, 0, 200000));
        assertThat(zeroRate).isEqualTo(new PriceResponse(200000, 0, 200000));
        then(userRepository).shouldHaveNoInteractions();
    }

    @Test
    void roundsDiscountedPriceHalfUpToWon() {
        // 100,005 × 0.9 = 90,004.5 => 90,005
        TourStyle style = TourStyle.builder().tourId(1L).type(GRAND).basePrice(100005)
                .regularDiscount(0.1f).build();
        given(userRepository.findById(10L)).willReturn(Optional.of(user(true)));

        PriceResponse result = tourService.calculatePriceByIds(Tour.builder().build(),
                style, hotel(_3_STAR), food(LUNCHBOX), List.of(), 1, 10L);

        assertThat(result).isEqualTo(new PriceResponse(100005, 10000, 90005));
    }

    private void stubRequestedPriceOptions(TourStyle style) {
        given(tourRepository.findById(1L)).willReturn(Optional.of(Tour.builder().build()));
        given(tourStyleRepository.findById(2L)).willReturn(Optional.of(style));
        given(hotelOptionRepository.findById(3L)).willReturn(Optional.of(hotel(_3_STAR)));
        given(foodOptionRepository.findById(4L)).willReturn(Optional.of(food(LUNCHBOX)));
    }

    private PriceCalculateRequest priceRequest() {
        return new PriceCalculateRequest(2L, 3L, 4L, 2, List.of());
    }

    private TourStyle discountedStyle(Float regularDiscount) {
        return TourStyle.builder().tourId(1L).type(GRAND).basePrice(100000)
                .regularDiscount(regularDiscount).build();
    }

    private User user(boolean isRegular) {
        User user = User.builder().loginId("user").build();
        user.updateIsRegular(isRegular);
        return user;
    }

    private TourStyle style(TourStyleType type) {
        return TourStyle.builder().tourId(1L).type(type).basePrice(100000).build();
    }

    private HotelOption hotel(HotelGrade grade) {
        return HotelOption.builder().tourId(1L).grade(grade).price(30000).build();
    }

    private FoodOption food(FoodType type) {
        return FoodOption.builder().tourId(1L).type(type).price(10000).build();
    }

    // Base: 100,000, hotel: 30,000, food: 10,000 per person; two participants.
    // Expected totals are explicit so the tests do not repeat the production
    // comparison.
    static Stream<Arguments> gradeCases() {
        return Stream.of(
                arguments(GRAND, _3_STAR, LUNCHBOX, 200000),
                arguments(GRAND, _4_STAR, LOCAL_RESTAURANT, 200000),
                arguments(GRAND, _5_STAR, LOCAL_RESTAURANT, 260000),
                arguments(GRAND, _4_STAR, PREMIUM_RESTAURANT, 220000),
                arguments(GRAND, _5_STAR, PREMIUM_RESTAURANT, 280000));
    }
}
