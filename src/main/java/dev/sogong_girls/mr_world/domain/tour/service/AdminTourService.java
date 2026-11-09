package dev.sogong_girls.mr_world.domain.tour.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
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

    private void validatePlan(TourCreateRequest request) {
        if (request.tourStyles() == null) {
            return;
        }

        EnumSet<TourStyleType> styles = EnumSet.noneOf(TourStyleType.class);
        for (TourCreateRequest.TourStyleRequest style : request.tourStyles()) {
            if (style == null || style.type() == null) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT, "투어 스타일이 필요합니다.");
            }
            if (!styles.add(style.type())) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT, "투어 스타일은 중복될 수 없습니다.");
            }
            if ((request.tourTheme() == TourTheme.HONEYMOON || request.tourTheme() == TourTheme.PARENT)
                    && style.type() == TourStyleType.CLASSIC) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT,
                        "허니문, 효도 여행은 Classic 스타일을 선택할 수 없습니다.");
            }

            int grade = style.type().getGrade();
            if (style.hotelOption() != null
                    && (style.hotelOption().grade() == null || style.hotelOption().grade().getGrade() != grade)) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT,
                        "투어 스타일과 호텔 옵션의 등급이 일치하지 않습니다.");
            }
            if (style.foodOption() != null
                    && (style.foodOption().type() == null || style.foodOption().type().getGrade() != grade)) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT,
                        "투어 스타일과 식사 옵션의 등급이 일치하지 않습니다.");
            }
        }
    }

    private void validatePlan(Tour tour, List<TourStyle> tourStyles,
            Map<Long, HotelOption> hotels, Map<Long, FoodOption> foods) {
        EnumSet<TourStyleType> styles = EnumSet.noneOf(TourStyleType.class);
        for (TourStyle style : tourStyles) {
            if (style.getType() == null || !styles.add(style.getType())) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT, "투어 스타일은 중복될 수 없습니다.");
            }
            if ((tour.getTourTheme() == TourTheme.HONEYMOON || tour.getTourTheme() == TourTheme.PARENT)
                    && style.getType() == TourStyleType.CLASSIC) {
                throw new BusinessException(BusinessErrorCode.INVALID_INPUT,
                        "허니문, 효도 여행은 Classic 스타일을 선택할 수 없습니다.");
            }

            int grade = style.getType().getGrade();
            if (style.getHotelOptionId() != null) {
                HotelOption hotel = hotels.get(style.getHotelOptionId());
                if (hotel == null || hotel.getGrade() == null || hotel.getGrade().getGrade() != grade) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT,
                            "투어 스타일과 호텔 옵션의 등급이 일치하지 않습니다.");
                }
            }
            if (style.getFoodOptionId() != null) {
                FoodOption food = foods.get(style.getFoodOptionId());
                if (food == null || food.getType() == null || food.getType().getGrade() != grade) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT,
                            "투어 스타일과 식사 옵션의 등급이 일치하지 않습니다.");
                }
            }
        }
    }

    @Transactional
    public TourResponse createTour(TourCreateRequest request) {
        // 생성 가능한 옵션 및 조건을 충족하는지 검증
        validatePlan(request);

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
        // 검증 전에 자동 flush가 발생하지 않도록 수정할 엔티티 먼저 조회
        List<TourStyle> tourStyles = tourStyleRepository.findAllByTourId(id);
        Map<Long, HotelOption> hotels = hotelOptionRepository.findAllByTourId(id).stream()
                .collect(Collectors.toMap(HotelOption::getId, Function.identity()));
        Map<Long, FoodOption> foods = foodOptionRepository.findAllByTourId(id).stream()
                .collect(Collectors.toMap(FoodOption::getId, Function.identity()));

        tour.updateInfo(request.name(), request.departure(), request.destination(),
                request.tourTheme(), request.transportType(), request.souvenirId(),
                request.nights(), request.days(), request.departureTime(), request.arrivalTime(),
                request.course(), request.description());

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
                    if (hotelRequest.id() == null) {
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
                    if (foodRequest.id() == null) {
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

        validatePlan(tour, tourStyles, hotels, foods);

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
