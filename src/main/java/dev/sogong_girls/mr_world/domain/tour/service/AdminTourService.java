package dev.sogong_girls.mr_world.domain.tour.service;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.tour.dto.TourCreateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourUpdateRequest;
import dev.sogong_girls.mr_world.domain.tour.repository.AddonOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.FoodOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.HotelOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourStyleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTourService {
    private final TourRepository tourRepository;
    private final TourStyleRepository tourStyleRepository;
    private final HotelOptionRepository hotelOptionRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final AddonOptionRepository addonOptionRepository;

    private boolean validatePlan(Long id) {
        throw new UnsupportedOperationException("validatePlan is not implemented yet");
    }

    public TourResponse createTour(TourCreateRequest request) {
        throw new UnsupportedOperationException("createTour is not implemented yet");
    }

    public TourResponse updateTour(Long id, TourUpdateRequest request) {
        throw new UnsupportedOperationException("updateTour is not implemented yet");
    }
}
