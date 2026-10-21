package dev.sogong_girls.mr_world.domain.tour.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.tour.dto.TourCreateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourUpdateRequest;
import dev.sogong_girls.mr_world.domain.tour.service.AdminTourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminTourController {
    private final AdminTourService service;

    @PostMapping("/api/admin/tours")
    public ResponseEntity<TourResponse> createTour(@Valid @RequestBody TourCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTour(request));
    }

    @PatchMapping("/api/admin/tours/{id}")
    public ResponseEntity<TourResponse> updateTour(@PathVariable("id") Long id,
            @Valid @RequestBody TourUpdateRequest request) {
        return ResponseEntity.ok(service.updateTour(id, request));
    }
}
