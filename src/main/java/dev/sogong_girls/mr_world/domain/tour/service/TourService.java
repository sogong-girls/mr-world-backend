package dev.sogong_girls.mr_world.domain.tour.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.tour.dto.PriceCalculateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourDetailResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;

@Service
public class TourService {
    public List<TourResponse> getTourList() {
        throw new UnsupportedOperationException("getTourList is not implemented yet");
    }

    public TourDetailResponse getTour(Long id) {
        throw new UnsupportedOperationException("getTour is not implemented yet");
    }

    public PriceResponse calculatePrice(Long id, PriceCalculateRequest request) {
        throw new UnsupportedOperationException("calculatePrice is not implemented yet");
    }
}
