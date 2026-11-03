package dev.sogong_girls.mr_world.domain.souvenir.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirCreateRequest;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirUpdateRequest;
import dev.sogong_girls.mr_world.domain.souvenir.dto.StockUpdateRequest;
import dev.sogong_girls.mr_world.domain.souvenir.entity.Souvenir;
import dev.sogong_girls.mr_world.domain.souvenir.mapper.SouvenirMapper;
import dev.sogong_girls.mr_world.domain.souvenir.repository.SouvenirRepository;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSouvenirService {
    private final SouvenirRepository souvenirRepository;

    private final SouvenirMapper souvenirMapper;

    public List<SouvenirResponse> getSouvenirList() {
        List<Souvenir> souvenirs = souvenirRepository.findAll();

        // TODO: 대시보드에 들어갈 각 예약별 필요 수량 구하기
        // (대시보드 형식을 알아야 함)

        return souvenirMapper.toResponses(souvenirs);
    }

    @Transactional
    public SouvenirResponse createSouvenir(SouvenirCreateRequest request) {
        Souvenir souvenir = souvenirRepository.save(Souvenir.builder()
                .name(request.name())
                .brand(request.brand())
                .description(request.description())
                .stockCount(request.stockCount())
                .build());

        return souvenirMapper.toResponse(souvenir);
    }

    @Transactional
    public SouvenirResponse updateSouvenir(Long id, SouvenirUpdateRequest request) {
        Souvenir souvenir = souvenirRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        souvenir.updateInfo(request.name(), request.brand(), request.description());
        return souvenirMapper.toResponse(souvenir);
    }

    @Transactional
    public SouvenirResponse updateStock(Long id, StockUpdateRequest request) {
        Souvenir souvenir = souvenirRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        souvenir.updateStockCount(request.amount());
        return souvenirMapper.toResponse(souvenir);
    }

    private Integer calaulateRequired(Long id) {
        throw new UnsupportedOperationException("calculateRequired is not implemented yet");
    }

    @Transactional
    private Integer increaseStock(Souvenir souvenir, Integer amount) {
        souvenir.updateStockCount(amount);
        return souvenir.getStockCount();
    }

    @Transactional
    private Integer decreaseStock(Souvenir souvenir, Integer amount) {
        souvenir.updateStockCount(amount);
        return souvenir.getStockCount();
    }
}
