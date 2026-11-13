package dev.sogong_girls.mr_world.domain.booking.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingStatusUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.SmsNotificationResponse;
import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.booking.mapper.BookingMapper;
import dev.sogong_girls.mr_world.domain.booking.mapper.SmsNotificationMapper;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingRepository;
import dev.sogong_girls.mr_world.domain.booking.repository.SmsNotificationRepository;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBookingService {
    /**
     * 출발/도착 시각에 따라 상태가 바뀔 수 있는 예약의 상태 (결제 완료 후 아직 도착하지 않음)
     */
    private static final Set<TourStatus> IN_PROGRESS_STATUSES = EnumSet.of(
            TourStatus.BOOKED, TourStatus.DEPARTED);

    private final BookingRepository bookingRepository;
    private final SmsNotificationRepository smsNotificationRepository;
    private final BookingMapper bookingMapper;
    private final SmsNotificationMapper smsNotificationMapper;

    public List<BookingResponse> getAllBookingList() {
        return bookingMapper.toResponses(bookingRepository.findAll());
    }

    public BookingDetailResponse getAdminBooking(Long id) {
        Booking booking = bookingRepository.findWithOptionsById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        return bookingMapper.toDetailResponse(booking);
    }

    @Transactional
    public BookingResponse updateAdminBookingStatus(Long id, BookingStatusUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        booking.updateStatus(request.status());
        return bookingMapper.toResponse(booking);
    }

    /**
     * 1시간마다 결제 완료된 예약의 출발/도착 시각을 확인해 상태를 갱신한다.
     * BOOKED -> DEPARTED (출발 시각 경과), BOOKED/DEPARTED -> ARRIVED (도착 시각 경과)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateTourProgressStatuses() {
        updateTourProgressStatuses(LocalDateTime.now());
    }

    void updateTourProgressStatuses(LocalDateTime now) {
        // 출발 시각이 지나지 않은 예약은 바뀔 상태가 없으므로 조회하지 않는다
        List<Booking> bookings = bookingRepository
                .findAllByStatusInAndStartAtLessThanEqual(IN_PROGRESS_STATUSES, now);

        for (Booking booking : bookings) {
            if (!booking.getEndAt().isAfter(now)) {
                // 스케줄러가 멈춰 있던 사이 도착 시각까지 지난 BOOKED 예약도 바로 ARRIVED로 전환
                booking.updateStatus(TourStatus.ARRIVED);
            } else if (booking.getStatus() == TourStatus.BOOKED) {
                booking.updateStatus(TourStatus.DEPARTED);
            }
        }
    }

    public List<SmsNotificationResponse> getSmsNotificationList() {
        return smsNotificationMapper.toResponses(smsNotificationRepository.findAll());
    }
}
