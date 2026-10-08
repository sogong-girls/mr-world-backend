package dev.sogong_girls.mr_world.domain.tour.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.tour.dto.PriceCalculateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.PriceResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourDetailResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TourController {
    private final TourService service;

    @GetMapping("/api/tours")
    public List<TourResponse> getTourList() {
        return service.getTourList();
    }

    @GetMapping("/api/tours/{id}")
    public TourDetailResponse getTour(@PathVariable("id") Long id) {
        return service.getTour(id);
    }

    @PostMapping("/api/tours/{id}/price")
    public PriceResponse calculatePrice(@PathVariable("id") Long id,
            @Valid @RequestBody PriceCalculateRequest request) {
        return service.calculatePrice(id, request);
    }
}
