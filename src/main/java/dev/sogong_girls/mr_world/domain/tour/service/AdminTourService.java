package dev.sogong_girls.mr_world.domain.tour.service;

import dev.sogong_girls.mr_world.domain.tour.dto.TourCreateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourStyleRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.TourStyleResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourUpdateRequest;
import org.springframework.stereotype.Service;

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
}
