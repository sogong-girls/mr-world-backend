package dev.sogong_girls.mr_world.domain.tour.service;

import dev.sogong_girls.mr_world.domain.tour.dto.AddonOptionRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.AddonOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.FoodOptionRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.FoodOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.HotelOptionRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.HotelOptionResponse;
import org.springframework.stereotype.Service;

@Service
public class AdminOptionService {
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
