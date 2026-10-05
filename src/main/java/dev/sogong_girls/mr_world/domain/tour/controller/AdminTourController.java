package dev.sogong_girls.mr_world.domain.tour.controller;

import dev.sogong_girls.mr_world.domain.tour.dto.TourCreateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.TourResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourStyleRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.TourStyleResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.TourUpdateRequest;
import dev.sogong_girls.mr_world.domain.tour.service.AdminTourService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminTourController {
    private final AdminTourService service;

    public AdminTourController(AdminTourService service) {
        this.service = service;
    }

    @PostMapping("/api/admin/tours")
    @ResponseStatus(HttpStatus.CREATED)
    public TourResponse createTour(@Valid @RequestBody TourCreateRequest request) {
        return service.createTour(request);
    }

    @PatchMapping("/api/admin/tours/{id}")
    public TourResponse updateTour(@PathVariable("id") Long id, @Valid @RequestBody TourUpdateRequest request) {
        return service.updateTour(id, request);
    }

    @DeleteMapping("/api/admin/tours/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTour(@PathVariable("id") Long id) {
        service.deleteTour(id);
    }

    @PostMapping("/api/admin/tours/{id}/styles")
    @ResponseStatus(HttpStatus.CREATED)
    public TourStyleResponse createTourStyle(@PathVariable("id") Long id, @Valid @RequestBody TourStyleRequest request) {
        return service.createTourStyle(id, request);
    }

    @PatchMapping("/api/admin/tour-styles/{id}")
    public TourStyleResponse updateTourStyle(@PathVariable("id") Long id, @Valid @RequestBody TourStyleRequest request) {
        return service.updateTourStyle(id, request);
    }
}
