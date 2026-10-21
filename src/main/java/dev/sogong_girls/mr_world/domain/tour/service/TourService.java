package dev.sogong_girls.mr_world.domain.tour.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.tour.dto.AddonOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.FoodOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.HotelOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceCalculateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourDetailResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourStyleResponse;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
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
        Tour tour = tourRepository.findById(id).orElse(null);
        if (tour == null)
            throw new BusinessException(BusinessErrorCode.NOT_FOUND);

        List<TourStyleResponse> styles = tourMapper.toTourStyleResponses(tourStyleRepository.findAllByTourId(id));
        List<HotelOptionResponse> hotelOptions = tourMapper
                .toHotelOptionResponses(hotelOptionRepository.findAllByTourId(id));
        List<FoodOptionResponse> foodOptions = tourMapper
                .toFoodOptionResponses(foodOptionRepository.findAllByTourId(id));
        List<AddonOptionResponse> addonOptions = tourMapper
                .toAddonOptionResponses(addonOptionRepository.findAllByTourId(id));

        return new TourDetailResponse(
                tour.getId(), tour.getName(), tour.getDeparture(), tour.getDestination(),
                tour.getTourTheme(), tour.getTransportType(), tour.getSouvenirId(),
                tour.getNNights(), tour.getNDays(), tour.getDepartureTime(), tour.getArrivalTime(),
                tour.getCourse(), tour.getDescription(),
                styles, hotelOptions, foodOptions, addonOptions);
    }

    public PriceResponse calculatePrice(Long id, PriceCalculateRequest request) {
        throw new UnsupportedOperationException("calculatePrice is not implemented yet");
    }

    private boolean validateOptions(PriceCalculateRequest request) {
        throw new UnsupportedOperationException("validateOptions is not implemented yet");
    }

    private Integer calculateTotal(PriceCalculateRequest request) {
        throw new UnsupportedOperationException("calculateTotal is not implemented yet");
    }

    private Integer applyRegularDiscount(Integer totalPrice) {
        throw new UnsupportedOperationException("applyRegularDiscount is not implemented yet");
    }
}
