package dev.sogong_girls.mr_world.domain.tour.service;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.tour.dto.AddonOptionRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.AddonOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.FoodOptionRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.FoodOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.HotelOptionRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.HotelOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourCreateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourStyleRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.TourStyleResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourUpdateRequest;

@Service
public class AdminTourService {
    public TourResponse createTour(TourCreateRequest request) {
        throw new UnsupportedOperationException("createTour is not implemented yet");
    }

    public TourResponse updateTour(Long id, TourUpdateRequest request) {
        throw new UnsupportedOperationException("updateTour is not implemented yet");
    }

    public void deleteTour(Long id) {
        throw new UnsupportedOperationException("deleteTour is not implemented yet");
    }

    public TourStyleResponse createTourStyle(Long id, TourStyleRequest request) {
        throw new UnsupportedOperationException("createTourStyle is not implemented yet");
    }

    public TourStyleResponse updateTourStyle(Long id, TourStyleRequest request) {
        throw new UnsupportedOperationException("updateTourStyle is not implemented yet");
    }

    public void deleteTourStyle(Long id) {
        throw new UnsupportedOperationException("deleteTourStyle is not implemented yet");
    }

    public HotelOptionResponse createHotelOption(Long id, HotelOptionRequest request) {
        throw new UnsupportedOperationException("createHotelOption is not implemented yet");
    }

    public FoodOptionResponse createFoodOption(Long id, FoodOptionRequest request) {
        throw new UnsupportedOperationException("createFoodOption is not implemented yet");
    }

    public AddonOptionResponse createAddonOption(Long id, AddonOptionRequest request) {
        throw new UnsupportedOperationException("createAddonOption is not implemented yet");
    }
}
