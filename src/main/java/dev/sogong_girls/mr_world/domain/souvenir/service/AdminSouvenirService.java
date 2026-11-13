package dev.sogong_girls.mr_world.domain.souvenir.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingRepository;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirCreateRequest;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirStockResponse;
import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirStockResponse.BookingSouvenirRequirement;
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
    private final BookingRepository bookingRepository;

    private final SouvenirMapper souvenirMapper;

    /**
     * 기념품을 보내야 하는 예약 상태 (확정 + 결제 완료, 출발 전)
     */
    private static final Set<TourStatus> SOUVENIR_REQUIRED_STATUSES = EnumSet.of(TourStatus.CONFIRMED,
            TourStatus.BOOKED);

    @Transactional(readOnly = true)
    public List<SouvenirStockResponse> getSouvenirList() {
        List<Souvenir> souvenirs = souvenirRepository.findAll();

        // 기념품 ID -> 예약별 필요 수량 (참가 인원 1인당 1개)
        Map<Long, List<BookingSouvenirRequirement>> requirementsBySouvenirId = bookingRepository
                .findAllWithSouvenirByStatusIn(SOUVENIR_REQUIRED_STATUSES).stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getTour().getSouvenirId(),
                        Collectors.mapping(this::toRequirement, Collectors.toList())));

        return souvenirs.stream()
                .map(souvenir -> {
                    List<BookingSouvenirRequirement> requirements = requirementsBySouvenirId
                            .getOrDefault(souvenir.getId(), List.of());
                    int requiredCount = requirements.stream()
                            .mapToInt(BookingSouvenirRequirement::quantity)
                            .sum();

                    return new SouvenirStockResponse(
                            souvenir.getId(),
                            souvenir.getName(),
                            souvenir.getBrand(),
                            souvenir.getDescription(),
                            souvenir.getStockCount(),
                            requiredCount,
                            requirements);
                })
                .toList();
    }

    private BookingSouvenirRequirement toRequirement(Booking booking) {
        return new BookingSouvenirRequirement(
                booking.getId(),
                booking.getTour().getId(),
                booking.getStartAt(),
                booking.getStatus(),
                booking.getParticipantsCount());
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
