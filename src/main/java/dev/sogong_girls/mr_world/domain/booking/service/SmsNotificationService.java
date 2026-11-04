package dev.sogong_girls.mr_world.domain.booking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.dto.SmsNotificationResponse;
import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.booking.mapper.SmsNotificationMapper;
import dev.sogong_girls.mr_world.domain.booking.repository.SmsNotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmsNotificationService {
    private final SmsNotificationRepository smsNotificationRepository;
    private final SmsNotificationMapper smsNotificationMapper;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkNotificationsSendDue() {
        List<SmsNotification> unpaidBookingNotifications = smsNotificationRepository
                .findUnsentForUnpaidBookings(TourStatus.CONFIRMED);
        for (SmsNotification notification : unpaidBookingNotifications) {
            sendNotice();
            notification.updateSentAt();
        }
        List<SmsNotification> notifications = smsNotificationRepository
                .findAllBySentAtIsNullAndSendDueLessThanEqual(LocalDateTime.now());
        for (SmsNotification notification : notifications) {
            sendNotice();
            notification.updateSentAt();
        }
    }

    @Transactional
    public List<SmsNotificationResponse> createNotifications(Booking booking) {
        List<SmsNotification> notifications = new ArrayList<>();
        if (booking.getStatus() == TourStatus.PENDING) {
            notifications.add(SmsNotification.builder()
                    .bookingId(booking.getId())
                    .message("예약이 확정되었습니다. 결제를 진행해 주세요.")
                    .build());
        }

        notifications.add(SmsNotification.builder()
                .bookingId(booking.getId())
                .sendDue(booking.getStartAt().minusHours(24))
                .message("여행 출발 24시간 전 안내입니다.")
                .build());

        return smsNotificationMapper.toResponses(smsNotificationRepository.saveAll(notifications));
    }

    public void sendNotice() {
        throw new UnsupportedOperationException("sendNotice is not implemented yet");
    }
}
