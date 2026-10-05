package dev.sogong_girls.mr_world.domain.tour.controller;

import dev.sogong_girls.mr_world.domain.tour.dto.AddonOptionRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.AddonOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.FoodOptionRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.FoodOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.HotelOptionRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.HotelOptionResponse;
import dev.sogong_girls.mr_world.domain.tour.service.AdminOptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminOptionController {
    private final AdminOptionService service;

    public AdminOptionController(AdminOptionService service) {
        this.service = service;
    }

    @PostMapping("/api/admin/tours/{id}/hotel-options")
    @ResponseStatus(HttpStatus.CREATED)
    public HotelOptionResponse createHotelOption(@PathVariable("id") Long id, @Valid @RequestBody HotelOptionRequest request) {
        return service.createHotelOption(id, request);
    }

    @PostMapping("/api/admin/tours/{id}/food-options")
    @ResponseStatus(HttpStatus.CREATED)
    public FoodOptionResponse createFoodOption(@PathVariable("id") Long id, @Valid @RequestBody FoodOptionRequest request) {
        return service.createFoodOption(id, request);
    }

    @PostMapping("/api/admin/tours/{id}/addon-options")
    @ResponseStatus(HttpStatus.CREATED)
    public AddonOptionResponse createAddonOption(@PathVariable("id") Long id, @Valid @RequestBody AddonOptionRequest request) {
        return service.createAddonOption(id, request);
    }
}
