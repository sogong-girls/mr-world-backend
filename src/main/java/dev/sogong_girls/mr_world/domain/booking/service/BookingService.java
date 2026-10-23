package dev.sogong_girls.mr_world.domain.booking.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingCreateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingOptionUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.entity.BookingAddon;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.booking.mapper.BookingMapper;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingAddonRepository;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingRepository;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceCalculateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceResponse;
import dev.sogong_girls.mr_world.domain.tour.entity.AddonOption;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;
import dev.sogong_girls.mr_world.domain.tour.repository.AddonOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.FoodOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.HotelOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourStyleRepository;
import dev.sogong_girls.mr_world.domain.tour.service.TourService;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingAddonRepository bookingAddonRepository;
    private final AddonOptionRepository addonOptionRepository;
    private final TourRepository tourRepository;
    private final TourStyleRepository tourStyleRepository;
    private final HotelOptionRepository hotelOptionRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final TourService tourService;

    private final BookingMapper bookingMapper;

    @Transactional
    public BookingDetailResponse createBooking(Long userId, BookingCreateRequest request) {
        // 1단계: Booking 객체 생성
        Tour tour = tourRepository.findById(request.tourId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        TourStyle style = tourStyleRepository.findById(request.tourStyleId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        HotelOption hotel = hotelOptionRepository.findById(request.hotelOptionId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        FoodOption food = foodOptionRepository.findById(request.foodOptionId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        List<BookingCreateRequest.BookingAddonRequest> addonRequests = request.bookingAddons() == null ? List.of()
                : request.bookingAddons();
        List<AddonOption> addonOptions = new ArrayList<>();
        for (BookingCreateRequest.BookingAddonRequest addonRequest : addonRequests) {
            if (addonRequest == null || addonRequest.addonOptionId() == null) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
            }
            addonOptions.add(addonOptionRepository.findById(addonRequest.addonOptionId())
                    .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND)));
        }

        // 금액 및 상태 결정
        PriceResponse price = tourService.calculatePrice(tour.getId(), new PriceCalculateRequest(
                style.getId(), hotel.getId(), food.getId(), request.participantsCount(),
                addonOptions.stream().map(AddonOption::getId).toList()));

        TourStatus status = request.participantsCount() >= 3 ? TourStatus.CONFIRMED : TourStatus.PENDING;

        Booking booking = bookingRepository.save(Booking.builder()
                .tour(tour)
                .userId(userId)
                .tourStyleId(style.getId())
                .tourStyleType(style.getType())
                .hotelOption(hotel)
                .foodOption(food)
                .totalPrice(price.totalPrice())
                .discountPrice(price.discountPrice())
                .participantsCount(request.participantsCount())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .nights(tour.getNights())
                .days(tour.getDays())
                .status(status)
                .build());

        // 2단계: BookingAddon 리스트 생성
        List<BookingAddon> bookingAddons = new ArrayList<>();
        for (int i = 0; i < addonRequests.size(); i++) {
            BookingCreateRequest.BookingAddonRequest addonRequest = addonRequests.get(i);
            bookingAddons.add(bookingAddonRepository.save(BookingAddon.builder()
                    .bookingId(booking.getId())
                    .addonOption(addonOptions.get(i))
                    .nthDay(addonRequest.nthDay())
                    .meal(addonRequest.meal())
                    .build()));
        }

        // 3단계: 생성된 addons를 Booking에 반영
        booking.updateBookingAddons(bookingAddons);

        return bookingMapper.toDetailResponse(booking);
    }

    @Transactional
    public BookingDetailResponse updateBookingOptions(Long id, Long userId, BookingOptionUpdateRequest request) {
        // 1단계: Booking 옵션 수정
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        if (!booking.getUserId().equals(userId))
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);

        Long tourId = booking.getTour().getId();
        Long styleId = request.tourStyleId();
        Long hotelId = request.hotelOptionId();
        Long foodId = request.foodOptionId();

        TourStyle style = tourStyleRepository.findById(styleId).orElse(null);
        HotelOption hotel = hotelOptionRepository.findById(hotelId).orElse(null);
        FoodOption food = foodOptionRepository.findById(foodId).orElse(null);
        booking.updateOptions(styleId, style == null ? null : style.getType(), hotel, food, null, null);

        // 2단계: 요청된 BookingAddon 리스트 수정
        if (request.bookingAddons() != null) {
            for (BookingOptionUpdateRequest.BookingAddonUpdateRequest addonRequest : request.bookingAddons()) {
                if (addonRequest == null || addonRequest.id() == null) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                }
                BookingAddon bookingAddon = bookingAddonRepository.findById(addonRequest.id())
                        .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
                if (!id.equals(bookingAddon.getBookingId())) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                }

                AddonOption addonOption = null;
                if (addonRequest.addonOptionId() != null) {
                    addonOption = addonOptionRepository.findById(addonRequest.addonOptionId())
                            .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
                    if (!tourId.equals(addonOption.getTourId())) {
                        throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                    }
                }
                bookingAddon.updateInfo(addonOption, addonRequest.nthDay(), addonRequest.meal());
            }
        }

        // 3단계: 수정된 addons와 금액을 Booking에 반영
        List<BookingAddon> bookingAddons = bookingAddonRepository.findAllByBookingId(id);
        booking.updateBookingAddons(bookingAddons);

        PriceResponse price = tourService.calculatePrice(tourId, new PriceCalculateRequest(
                styleId, hotelId, foodId, booking.getParticipantsCount(),
                bookingAddons.stream().map(addon -> addon.getAddonOption().getId()).toList()));
        booking.updateOptions(null, null, null, null, price.totalPrice(), price.discountPrice());

        return bookingMapper.toDetailResponse(booking);
    }

    @Transactional
    public BookingResponse payBooking(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        if (!booking.getUserId().equals(userId))
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);

        booking.updatePaidAt();
        return bookingMapper.toResponse(booking);
    }

    private boolean validateBooking(Long id) {
        throw new UnsupportedOperationException("validateBooking is not implemented yet");
    }

    private boolean checkPayable(Long id) {
        throw new UnsupportedOperationException("checkPayable is not implemented yet");
    }

    private boolean processPayment(Long id) {
        throw new UnsupportedOperationException("processPayment is not implemented yet");
    }
}
