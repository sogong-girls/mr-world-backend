package dev.sogong_girls.mr_world.domain.tour.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.tour.dto.SouvenirCreateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.tour.dto.SouvenirUpdateRequest;
import dev.sogong_girls.mr_world.domain.tour.dto.StockUpdateRequest;
import dev.sogong_girls.mr_world.domain.tour.repository.SouvenirRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSouvenirService {
    private final SouvenirRepository souvenirRepository;

    public List<SouvenirResponse> getSouvenirList() {
        throw new UnsupportedOperationException("getSouvenirList is not implemented yet");
    }

    public SouvenirResponse createSouvenir(Long id, SouvenirCreateRequest request) {
        throw new UnsupportedOperationException("createSouvenir is not implemented yet");
    }

    public SouvenirResponse updateSouvenir(Long id, SouvenirUpdateRequest request) {
        throw new UnsupportedOperationException("updateSouvenir is not implemented yet");
    }

    public SouvenirResponse updateStock(Long id, StockUpdateRequest request) {
        throw new UnsupportedOperationException("updateStock is not implemented yet");
    }

    private Integer calaulateRequired(Long id) {
        throw new UnsupportedOperationException("calculateRequired is not implemented yet");
    }

    private Integer increaseStock(Long id, Integer amount) {
        throw new UnsupportedOperationException("increaseStock is not implemented yet");
    }

    private Integer decreaseStock(Long id, Integer amount) {
        throw new UnsupportedOperationException("decreaseStock is not implemented yet");
    }
}
