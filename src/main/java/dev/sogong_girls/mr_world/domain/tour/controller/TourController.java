package dev.sogong_girls.mr_world.domain.tour.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import dev.sogong_girls.mr_world.global.security.JwtTokenProvider.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TourController {
    private final TourService service;

    @GetMapping("/api/tours")
    public ResponseEntity<List<TourResponse>> getTourList() {
        return ResponseEntity.ok(service.getTourList());
    }

    @GetMapping("/api/tours/{id}")
    public ResponseEntity<TourDetailResponse> getTour(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getTour(id));
    }

    @PostMapping("/api/tours/{id}/price")
    public ResponseEntity<PriceResponse> calculatePrice(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody PriceCalculateRequest request) {
        // 비로그인 요청도 허용되므로 principal이 null일 수 있다 (이 경우 단골 할인 미적용)
        Long userId = principal == null ? null : principal.userId();
        return ResponseEntity.ok(service.calculatePrice(id, userId, request));
    }
}
