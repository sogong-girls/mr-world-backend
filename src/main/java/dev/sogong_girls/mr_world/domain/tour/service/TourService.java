package dev.sogong_girls.mr_world.domain.tour.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.souvenir.mapper.SouvenirMapper;
import dev.sogong_girls.mr_world.domain.souvenir.repository.SouvenirRepository;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceCalculateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceCalculateRequest.AddonRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourDetailResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
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
import dev.sogong_girls.mr_world.domain.user.entity.User;
import dev.sogong_girls.mr_world.domain.user.repository.UserRepository;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TourService {
    private final TourRepository tourRepository;
    private final TourStyleRepository tourStyleRepository;
    private final HotelOptionRepository hotelOptionRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final AddonOptionRepository addonOptionRepository;
    private final SouvenirRepository souvenirRepository;
    private final UserRepository userRepository;

    private final TourMapper tourMapper;
    private final SouvenirMapper souvenirMapper;

    public List<TourResponse> getTourList() {
        return tourMapper.toResponses(tourRepository.findAll());
    }

    public TourDetailResponse getTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        List<TourStyle> styles = tourStyleRepository.findAllByTourId(id);
        List<HotelOption> hotelOptions = hotelOptionRepository.findAllByTourId(id);
        List<FoodOption> foodOptions = foodOptionRepository.findAllByTourId(id);
        List<AddonOption> addonOptions = addonOptionRepository.findAllByTourId(id);
        SouvenirResponse souvenir = findSouvenir(tour.getSouvenirId());

        return new TourDetailResponse(
                tour.getId(), tour.getName(), tour.getDeparture(), tour.getDestination(),
                tour.getTourTheme(), tour.getTransportType(), souvenir,
                tour.getNights(), tour.getDays(), tour.getDepartureTime(), tour.getArrivalTime(),
                tour.getCourse(), tour.getDescription(),
                styles, hotelOptions, foodOptions, addonOptions);
    }

    /**
     * 투어에 기념품이 없거나 연결된 기념품을 찾을 수 없으면 null을 반환한다.
     */
    private SouvenirResponse findSouvenir(Long souvenirId) {
        if (souvenirId == null)
            return null;

        return souvenirRepository.findById(souvenirId)
                .map(souvenirMapper::toResponse)
                .orElse(null);
    }

    /**
     * @param userId 로그인한 사용자 ID. 비로그인 상태면 null이며, 이 경우 단골 할인은 적용되지 않는다.
     */
    public PriceResponse calculatePrice(Long id, Long userId, PriceCalculateRequest request) {
        tourRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        TourStyle tourStyle = tourStyleRepository.findById(request.tourStyleId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        HotelOption hotelOption = hotelOptionRepository.findById(request.hotelOptionId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        FoodOption foodOption = foodOptionRepository.findById(request.foodOptionId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        if (!id.equals(tourStyle.getTourId()) || !id.equals(hotelOption.getTourId())
                || !id.equals(foodOption.getTourId())) {
            throw new BusinessException(BusinessErrorCode.INVALID_INPUT, "해당 투어에 존재하지 않는 옵션입니다.");
        }

        int pricePerParticipant = tourStyle.getBasePrice();
        if (hotelOption.getGrade().getGrade() > tourStyle.getType().getGrade())
            pricePerParticipant = Math.addExact(pricePerParticipant, hotelOption.getPrice());
        if (foodOption.getType().getGrade() > tourStyle.getType().getGrade())
            pricePerParticipant = Math.addExact(pricePerParticipant, foodOption.getPrice());

        if (request.addonOptions() != null) {
            for (AddonRequest addonReq : request.addonOptions()) {
                if (addonReq.addonOptionId() == null) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT, "해당 투어에 존재하지 않는 추가 옵션입니다.");
                }
                AddonOption addon = addonOptionRepository.findById(addonReq.addonOptionId())
                        .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
                if (!id.equals(addon.getTourId())) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                }
                pricePerParticipant = Math.addExact(pricePerParticipant, addon.getPrice());
            }
        }

        int totalPrice = Math.multiplyExact(pricePerParticipant, request.participantsCount());
        return applyRegularDiscount(totalPrice, tourStyle, userId);
    }

    public PriceResponse calculatePriceByIds(Tour tour, TourStyle tourStyle, HotelOption hotelOption,
            FoodOption foodOption,
            List<AddonOption> addonOptions, Integer participantsCount, Long userId) {

        int pricePerParticipant = tourStyle.getBasePrice();
        if (hotelOption.getGrade().getGrade() > tourStyle.getType().getGrade())
            pricePerParticipant = Math.addExact(pricePerParticipant, hotelOption.getPrice());
        if (foodOption.getType().getGrade() > tourStyle.getType().getGrade())
            pricePerParticipant = Math.addExact(pricePerParticipant, foodOption.getPrice());

        if (addonOptions != null) {
            for (AddonOption addon : addonOptions) {
                pricePerParticipant = Math.addExact(pricePerParticipant, addon.getPrice());
            }
        }

        int totalPrice = Math.multiplyExact(pricePerParticipant, participantsCount);
        return applyRegularDiscount(totalPrice, tourStyle, userId);
    }

    /**
     * 단골 사용자라면 투어 스타일의 단골 할인율(regularDiscount)을 적용한다.
     * 최종 금액 = 전체 금액 × (1 - 할인율), 원 단위 미만은 반올림한다.
     */
    private PriceResponse applyRegularDiscount(int totalPrice, TourStyle tourStyle, Long userId) {
        Float discountRate = tourStyle.getRegularDiscount();
        if (discountRate == null || discountRate <= 0 || !isRegularUser(userId)) {
            return new PriceResponse(totalPrice, 0, totalPrice);
        }

        // float -> double 변환 시 생기는 오차(0.1f -> 0.10000000149...)를 피하기 위해 문자열로 변환
        BigDecimal rate = new BigDecimal(Float.toString(discountRate));
        int finalPrice = BigDecimal.valueOf(totalPrice)
                .multiply(BigDecimal.ONE.subtract(rate))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
        return new PriceResponse(totalPrice, totalPrice - finalPrice, finalPrice);
    }

    private boolean isRegularUser(Long userId) {
        if (userId == null)
            return false;

        return userRepository.findById(userId)
                .map(User::getIsRegular)
                .orElse(false);
    }
}
