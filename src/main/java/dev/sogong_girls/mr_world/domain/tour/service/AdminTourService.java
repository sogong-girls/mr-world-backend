package dev.sogong_girls.mr_world.domain.tour.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.tour.dto.TourCreateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourUpdateRequest;
import dev.sogong_girls.mr_world.domain.tour.entity.AddonOption;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;
import dev.sogong_girls.mr_world.domain.tour.mapper.TourMapper;
import dev.sogong_girls.mr_world.domain.tour.repository.AddonOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.FoodOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.HotelOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourStyleRepository;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTourService {
    private final TourRepository tourRepository;
    private final TourStyleRepository tourStyleRepository;
    private final HotelOptionRepository hotelOptionRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final AddonOptionRepository addonOptionRepository;
    private final TourMapper tourMapper;

    private boolean validatePlan(Long id) {
        throw new UnsupportedOperationException("validatePlan is not implemented yet");
    }

    @Transactional
    public TourResponse createTour(TourCreateRequest request) {
        // 1단계: Tour 객체 생성
        Tour tour = tourRepository.save(Tour.builder()
                .name(request.name())
                .departure(request.departure())
                .destination(request.destination())
                .tourTheme(request.tourTheme())
                .transportType(request.transportType())
                .souvenirId(request.souvenirId())
                .nights(request.nights())
                .days(request.days())
                .departureTime(request.departureTime())
                .arrivalTime(request.arrivalTime())
                .course(request.course())
                .description(request.description())
                .build());

        // 2단계: TourStyle 리스트의 각 객체 생성
        if (request.tourStyles() != null) {
            for (TourCreateRequest.TourStyleRequest styleRequest : request.tourStyles()) {
                // HotelOption, FoodOption 우선 생성 후 FK로 등록
                Long hotelOptionId = null;
                if (styleRequest.hotelOption() != null) {
                    TourCreateRequest.HotelOptionRequest hotelRequest = styleRequest.hotelOption();
                    HotelOption hotel = hotelOptionRepository.save(HotelOption.builder()
                            .tourId(tour.getId())
                            .name(hotelRequest.name())
                            .location(hotelRequest.location())
                            .grade(hotelRequest.grade())
                            .price(hotelRequest.price())
                            .description(hotelRequest.description())
                            .build());
                    hotelOptionId = hotel.getId();
                }

                Long foodOptionId = null;
                if (styleRequest.foodOption() != null) {
                    TourCreateRequest.FoodOptionRequest foodRequest = styleRequest.foodOption();
                    FoodOption food = foodOptionRepository.save(FoodOption.builder()
                            .tourId(tour.getId())
                            .name(foodRequest.name())
                            .type(foodRequest.type())
                            .price(foodRequest.price())
                            .description(foodRequest.description())
                            .build());
                    foodOptionId = food.getId();
                }

                tourStyleRepository.save(TourStyle.builder()
                        .tourId(tour.getId())
                        .type(styleRequest.type())
                        .basePrice(styleRequest.basePrice())
                        .regularDiscount(styleRequest.regularDiscount() == null
                                ? null
                                : styleRequest.regularDiscount().floatValue())
                        .hotelOptionId(hotelOptionId)
                        .foodOptionId(foodOptionId)
                        .description(styleRequest.description())
                        .build());
            }
        }

        // 3단계: AddonOption 리스트의 각 객체 생성
        if (request.addonOptions() != null) {
            for (TourCreateRequest.AddonOptionRequest addonRequest : request.addonOptions()) {
                addonOptionRepository.save(AddonOption.builder()
                        .tourId(tour.getId())
                        .type(addonRequest.type())
                        .price(addonRequest.price())
                        .description(addonRequest.description())
                        .build());
            }
        }

        return tourMapper.toResponse(tour);
    }

    @Transactional
    public TourResponse updateTour(Long id, TourUpdateRequest request) {
        // 1단계: Tour 객체 수정
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        tour.updateInfo(request.name(), request.departure(), request.destination(),
                request.tourTheme(), request.transportType(), request.souvenirId(),
                null, null, null, null, null, request.description());

        // 2단계: TourStyle 객체 및 포함된 HotelOption/FoodOption 수정
        if (request.tourStyles() != null) {
            for (TourUpdateRequest.TourStyleRequest styleRequest : request.tourStyles()) {
                if (styleRequest == null || styleRequest.id() == null) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                }
                TourStyle style = tourStyleRepository.findById(styleRequest.id())
                        .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
                if (!id.equals(style.getTourId())) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                }

                Long hotelOptionId = null;
                if (styleRequest.hotelOption() != null) {
                    TourUpdateRequest.HotelOptionRequest hotelRequest = styleRequest.hotelOption();
                    if (hotelRequest.id() == null || !hotelRequest.id().equals(style.getHotelOptionId())) {
                        throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                    }
                    HotelOption hotel = hotelOptionRepository.findById(hotelRequest.id())
                            .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
                    if (!id.equals(hotel.getTourId())) {
                        throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                    }
                    hotel.updateInfo(hotelRequest.name(), hotelRequest.location(),
                            hotelRequest.grade(), hotelRequest.price(), hotelRequest.description());
                    hotelOptionId = hotel.getId();
                }

                Long foodOptionId = null;
                if (styleRequest.foodOption() != null) {
                    TourUpdateRequest.FoodOptionRequest foodRequest = styleRequest.foodOption();
                    if (foodRequest.id() == null || !foodRequest.id().equals(style.getFoodOptionId())) {
                        throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                    }
                    FoodOption food = foodOptionRepository.findById(foodRequest.id())
                            .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
                    if (!id.equals(food.getTourId())) {
                        throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                    }
                    food.updateInfo(foodRequest.name(), foodRequest.type(),
                            foodRequest.price(), foodRequest.description());
                    foodOptionId = food.getId();
                }

                style.updateInfo(styleRequest.type(), styleRequest.basePrice(),
                        styleRequest.regularDiscount() == null
                                ? null
                                : styleRequest.regularDiscount().floatValue(),
                        hotelOptionId, foodOptionId, styleRequest.description());
            }
        }

        // 3단계: 수정이 필요한 AddonOption 객체들 수정
        if (request.addonOptions() != null) {
            for (TourUpdateRequest.AddonOptionRequest addonRequest : request.addonOptions()) {
                if (addonRequest == null || addonRequest.id() == null) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                }
                AddonOption addon = addonOptionRepository.findById(addonRequest.id())
                        .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
                if (!id.equals(addon.getTourId())) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                }
                addon.updateInfo(addonRequest.type(), addonRequest.price(), addonRequest.description());
            }
        }

        return tourMapper.toResponse(tour);
    }
}
