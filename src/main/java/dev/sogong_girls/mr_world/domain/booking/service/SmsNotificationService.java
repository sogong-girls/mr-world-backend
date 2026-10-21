package dev.sogong_girls.mr_world.domain.booking.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.booking.dto.SmsNotificationResponse;
import dev.sogong_girls.mr_world.domain.booking.repository.SmsNotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmsNotificationService {
    private final SmsNotificationRepository smsNotificationRepository;

    @Scheduled(cron = "0 0 * * * * *")
    private void checkNotificationsSendDue() {
        throw new UnsupportedOperationException("checkNotificationsSendDue is not implemented yet");

    }

    public SmsNotificationResponse sendPaymentNotice() {
        throw new UnsupportedOperationException("sendPaymentNotice is not implemented yet");
    }

    public SmsNotificationResponse sendDepartureNotice() {
        throw new UnsupportedOperationException("sendDepartureNotice is not implemented yet");
    }
}
