package dev.sogong_girls.mr_world.domain.tour.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.tour.dto.SouvenirCreateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.SouvenirUpdateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.StockUpdateRequest;
import dev.sogong_girls.mr_world.domain.tour.service.AdminSouvenirService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminSouvenirController {
    private final AdminSouvenirService service;

    @GetMapping("/api/admin/souvenirs")
    public List<SouvenirResponse> getSouvenirList() {
        return service.getSouvenirList();
    }

    @PostMapping("/api/admin/tours/{id}/souvenirs")
    @ResponseStatus(HttpStatus.CREATED)
    public SouvenirResponse createSouvenir(@PathVariable("id") Long id,
            @Valid @RequestBody SouvenirCreateRequest request) {
        return service.createSouvenir(id, request);
    }

    @PatchMapping("/api/admin/souvenirs/{id}")
    public SouvenirResponse updateSouvenir(@PathVariable("id") Long id,
            @Valid @RequestBody SouvenirUpdateRequest request) {
        return service.updateSouvenir(id, request);
    }

    @PatchMapping("/api/admin/souvenirs/{id}/stock")
    public SouvenirResponse updateStock(@PathVariable("id") Long id, @Valid @RequestBody StockUpdateRequest request) {
        return service.updateStock(id, request);
    }
}
