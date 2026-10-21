package dev.sogong_girls.mr_world.domain.tour.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.tour.dto.PriceCalculateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourDetailResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TourService {
    private final TourRepository tourRepository;

    public List<TourResponse> getTourList() {
        throw new UnsupportedOperationException("getTourList is not implemented yet");
    }

    public TourDetailResponse getTour(Long id) {
        throw new UnsupportedOperationException("getTour is not implemented yet");
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
