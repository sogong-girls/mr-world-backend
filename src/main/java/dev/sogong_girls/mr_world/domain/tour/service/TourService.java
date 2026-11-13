package dev.sogong_girls.mr_world.domain.tour.service;

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

    public PriceResponse calculatePrice(Long id, PriceCalculateRequest request) {
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
        return new PriceResponse(totalPrice, 0, totalPrice);
    }

    public PriceResponse calculatePriceByIds(Tour tour, TourStyle tourStyle, HotelOption hotelOption,
            FoodOption foodOption,
            List<AddonOption> addonOptions, Integer participantsCount) {

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
        return new PriceResponse(totalPrice, 0, totalPrice);
    }
}
