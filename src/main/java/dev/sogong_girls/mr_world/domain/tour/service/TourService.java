package dev.sogong_girls.mr_world.domain.tour.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.tour.dto.PriceCalculateRequest;
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

    private final TourMapper tourMapper;

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

        return new TourDetailResponse(
                tour.getId(), tour.getName(), tour.getDeparture(), tour.getDestination(),
                tour.getTourTheme(), tour.getTransportType(), tour.getSouvenirId(),
                tour.getNights(), tour.getDays(), tour.getDepartureTime(), tour.getArrivalTime(),
                tour.getCourse(), tour.getDescription(),
                styles, hotelOptions, foodOptions, addonOptions);
    }

    public PriceResponse calculatePrice(Long id, PriceCalculateRequest request) {
        tourRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        TourStyle style = tourStyleRepository.findById(request.tourStyleId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        HotelOption hotel = hotelOptionRepository.findById(request.hotelOptionId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        FoodOption food = foodOptionRepository.findById(request.foodOptionId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        if (!id.equals(style.getTourId()) || !id.equals(hotel.getTourId()) || !id.equals(food.getTourId())) {
            throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
        }

        int pricePerParticipant = Math.addExact(style.getBasePrice(), hotel.getPrice());
        pricePerParticipant = Math.addExact(pricePerParticipant, food.getPrice());

        if (request.bookingAddonIds() != null) {
            for (Long addonId : request.bookingAddonIds()) {
                if (addonId == null) {
                    throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
                }
                AddonOption addon = addonOptionRepository.findById(addonId)
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
}
