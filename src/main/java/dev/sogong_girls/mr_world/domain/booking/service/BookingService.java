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
import dev.sogong_girls.mr_world.domain.tour.dto.PriceResponse;
import dev.sogong_girls.mr_world.domain.tour.entity.AddonOption;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
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
    private final SmsNotificationService smsNotificationService;

    private final BookingMapper bookingMapper;

    @Transactional
    public BookingDetailResponse createBooking(Long userId, BookingCreateRequest request) {
        // 1단계: Booking 객체 생성
        Tour tour = tourRepository.findById(request.tourId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        List<BookingCreateRequest.BookingAddonRequest> addonRequests = request.bookingAddons() == null ? List.of()
                : request.bookingAddons();
        List<Long> addonOptionIds = new ArrayList<>();
        for (BookingCreateRequest.BookingAddonRequest addonRequest : addonRequests) {
            if (addonRequest == null || addonRequest.addonOptionId() == null) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
            }
            addonOptionIds.add(addonRequest.addonOptionId());
        }
        ValidatedOptions options = validateBooking(tour, request.tourStyleId(), request.hotelOptionId(),
                request.foodOptionId(), addonOptionIds);

        // 금액 및 상태 결정
        PriceResponse price = tourService.calculatePriceByIds(tour.getId(),
                options.style().getId(), options.hotel().getId(), options.food().getId(),
                addonOptionIds, request.participantsCount());

        TourStatus status = request.participantsCount() >= 3 ? TourStatus.CONFIRMED : TourStatus.PENDING;

        Booking booking = bookingRepository.save(Booking.builder()
                .tour(tour)
                .userId(userId)
                .tourStyleId(options.style().getId())
                .tourStyleType(options.style().getType())
                .hotelOption(options.hotel())
                .foodOption(options.food())
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
                    .addonOption(options.addonOptions().get(i))
                    .nthDay(addonRequest.nthDay())
                    .meal(addonRequest.meal())
                    .build()));
        }

        // 3단계: 생성된 addons를 Booking에 반영
        booking.updateBookingAddons(bookingAddons);
        smsNotificationService.createNotifications(booking);

        return bookingMapper.toDetailResponse(booking);
    }

    @Transactional
    public BookingDetailResponse updateBookingOptions(Long id, Long userId, BookingOptionUpdateRequest request) {
        // 1단계: Booking 옵션 수정
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        if (!booking.getUserId().equals(userId))
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        checkPayable(booking);

        Long tourId = booking.getTour().getId();
        TourStyle style = request.tourStyleId() == null ? null
                : tourStyleRepository.findById(request.tourStyleId())
                        .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        HotelOption hotel = request.hotelOptionId() == null ? null
                : hotelOptionRepository.findById(request.hotelOptionId())
                        .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        FoodOption food = request.foodOptionId() == null ? null
                : foodOptionRepository.findById(request.foodOptionId())
                        .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        booking.updateOptions(request.tourStyleId(), style == null ? null : style.getType(),
                hotel, food, null, null);

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
                }
                bookingAddon.updateInfo(addonOption, addonRequest.nthDay(), addonRequest.meal());
            }
        }

        // 3단계: 수정된 addons와 금액을 Booking에 반영
        List<BookingAddon> bookingAddons = bookingAddonRepository.findAllByBookingId(id);
        booking.updateBookingAddons(bookingAddons);

        List<Long> addonOptionIds = bookingAddons.stream()
                .map(addon -> addon.getAddonOption().getId()).toList();
        validateBooking(booking.getTour(), booking.getTourStyleId(), booking.getHotelOption().getId(),
                booking.getFoodOption().getId(), addonOptionIds);

        PriceResponse price = tourService.calculatePriceByIds(tourId,
                booking.getTourStyleId(), booking.getHotelOption().getId(),
                booking.getFoodOption().getId(), addonOptionIds, booking.getParticipantsCount());

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

    private ValidatedOptions validateBooking(Tour tour, Long styleId, Long hotelId, Long foodId,
            List<Long> addonOptionIds) {
        // 선택된 투어 옵션이 존재하는지, 모두 같은 투어에 속해 있는지 검사
        if (styleId == null || hotelId == null || foodId == null) {
            throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
        }
        TourStyle style = tourStyleRepository.findById(styleId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        HotelOption hotel = hotelOptionRepository.findById(hotelId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        FoodOption food = foodOptionRepository.findById(foodId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        Long tourId = tour.getId();
        if (!tourId.equals(style.getTourId()) || !tourId.equals(hotel.getTourId())
                || !tourId.equals(food.getTourId())) {
            throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
        }
        if ((tour.getTourTheme() == TourTheme.HONEYMOON || tour.getTourTheme() == TourTheme.PARENT)
                && style.getType() == TourStyleType.CLASSIC) {
            throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
        }

        List<AddonOption> addonOptions = new ArrayList<>();
        for (Long addonOptionId : addonOptionIds) {
            if (addonOptionId == null) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
            }
            AddonOption addon = addonOptionRepository.findById(addonOptionId)
                    .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
            if (!tourId.equals(addon.getTourId())) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
            }
            addonOptions.add(addon);
        }
        return new ValidatedOptions(style, hotel, food, addonOptions);
    }

    private record ValidatedOptions(TourStyle style, HotelOption hotel, FoodOption food,
            List<AddonOption> addonOptions) {
    }

    private void checkPayable(Booking booking) {
        if (booking.getPaidAt() != null
                || (booking.getStatus() != TourStatus.PENDING && booking.getStatus() != TourStatus.CONFIRMED)) {
            throw new BusinessException(BusinessErrorCode.ALREADY_PAID);
        }
    }

    private boolean processPayment(Long id) {
        throw new UnsupportedOperationException("processPayment is not implemented yet");
    }
}
