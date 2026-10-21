package dev.sogong_girls.mr_world.domain.souvenir.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirCreateRequest;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirUpdateRequest;
import dev.sogong_girls.mr_world.domain.souvenir.dto.StockUpdateRequest;
import dev.sogong_girls.mr_world.domain.souvenir.service.AdminSouvenirService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminSouvenirController {
    private final AdminSouvenirService service;

    @GetMapping("/api/admin/souvenirs")
    public ResponseEntity<List<SouvenirResponse>> getSouvenirList() {
        return ResponseEntity.ok(service.getSouvenirList());
    }

    @PostMapping("/api/admin/tours/{id}/souvenirs")
    public ResponseEntity<SouvenirResponse> createSouvenir(@PathVariable("id") Long id,
            @Valid @RequestBody SouvenirCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSouvenir(id, request));
    }

    @PatchMapping("/api/admin/souvenirs/{id}")
    public ResponseEntity<SouvenirResponse> updateSouvenir(@PathVariable("id") Long id,
            @Valid @RequestBody SouvenirUpdateRequest request) {
        return ResponseEntity.ok(service.updateSouvenir(id, request));
    }

    @PatchMapping("/api/admin/souvenirs/{id}/stock")
    public ResponseEntity<SouvenirResponse> updateStock(@PathVariable("id") Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(service.updateStock(id, request));
    }
}
