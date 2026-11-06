package dev.sogong_girls.mr_world.domain.booking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import dev.sogong_girls.mr_world.domain.booking.dto.SmsNotificationResponse;
import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.booking.mapper.SmsNotificationMapper;
import dev.sogong_girls.mr_world.domain.booking.repository.SmsNotificationRepository;
import dev.sogong_girls.mr_world.domain.user.entity.User;
import dev.sogong_girls.mr_world.domain.user.repository.UserRepository;
import dev.sogong_girls.mr_world.global.config.AppProperties;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmsNotificationService {
    private final SmsNotificationRepository smsNotificationRepository;
    private final UserRepository userRepository;
    private final SmsNotificationMapper smsNotificationMapper;
    private final AppProperties properties;

    @Scheduled(cron = "0 0 * * * *")
    public void checkNotificationsSendDue() {
        List<SmsNotification> unpaidBookingNotifications = smsNotificationRepository
                .findUnsentForUnpaidBookings(TourStatus.CONFIRMED);
        for (SmsNotification notification : unpaidBookingNotifications) {
            sendNoticeDev(notification);
        }
        List<SmsNotification> notifications = smsNotificationRepository
                .findAllBySentAtIsNullAndSendDueLessThanEqual(LocalDateTime.now());
        for (SmsNotification notification : notifications) {
            sendNoticeDev(notification);
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

        User user = userRepository.findById(booking.getUserId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        notifications.add(SmsNotification.builder()
                .bookingId(booking.getId())
                .phoneNumber(user.getPhoneNumber())
                .sendDue(booking.getStartAt().minusHours(24))
                .message("여행 출발 24시간 전 안내입니다.")
                .build());

        return smsNotificationMapper.toResponses(smsNotificationRepository.saveAll(notifications));
    }

    public void sendNotice() {
        throw new UnsupportedOperationException("sendNotice is not implemented yet");
    }

    @Transactional
    public void sendNoticeDev(SmsNotification smsNotification) {
        // MOCEAN API로 SMS 전송
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("Authorization", "Bearer " + properties.SMS_API_KEY().strip());
        body.add("mocean-from", properties.SMS_SENDER_ID());
        body.add("mocean-to", smsNotification.getPhoneNumber());
        body.add("mocean-text", smsNotification.getMessage());

        ResponseEntity<Void> response = RestClient.create()
                .post()
                .uri("https://rest.moceanapi.com/rest/2/sms")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        if (response.getStatusCode() == HttpStatus.ACCEPTED) {
            smsNotification.updateSentAt();
        }
    }
}
