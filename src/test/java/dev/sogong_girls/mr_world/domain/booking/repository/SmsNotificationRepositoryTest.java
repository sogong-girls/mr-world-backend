package dev.sogong_girls.mr_world.domain.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;

/**
 * 스케줄러처럼 트랜잭션 밖에서 호출했을 때 실제로 커밋되는지 확인하려고 테스트에 @Transactional을 걸지 않는다.
 */
@SpringBootTest
@ActiveProfiles("test")
class SmsNotificationRepositoryTest {
    @Autowired
    SmsNotificationRepository smsNotificationRepository;

    @AfterEach
    void cleanUp() {
        smsNotificationRepository.deleteAll();
    }

    @Test
    void markSentCommitsSentAtOutsideTransaction() {
        SmsNotification notification = smsNotificationRepository.save(SmsNotification.builder()
                .bookingId(1L).phoneNumber("821012345678").message("안내").build());
        LocalDateTime sentAt = LocalDateTime.of(2026, 11, 20, 9, 0);

        int updated = smsNotificationRepository.markSent(notification.getId(), sentAt);

        assertThat(updated).isEqualTo(1);
        assertThat(smsNotificationRepository.findById(notification.getId()))
                .get().extracting(SmsNotification::getSentAt).isEqualTo(sentAt);
    }

    @Test
    void markSentKeepsOtherColumnsAndIgnoresAlreadySentNotification() {
        SmsNotification notification = smsNotificationRepository.save(SmsNotification.builder()
                .bookingId(1L).phoneNumber("821012345678").message("안내").build());
        LocalDateTime firstSentAt = LocalDateTime.of(2026, 11, 20, 9, 0);
        smsNotificationRepository.markSent(notification.getId(), firstSentAt);

        int updated = smsNotificationRepository.markSent(notification.getId(), firstSentAt.plusHours(1));

        assertThat(updated).isZero();
        SmsNotification saved = smsNotificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(saved.getSentAt()).isEqualTo(firstSentAt);
        assertThat(saved.getPhoneNumber()).isEqualTo("821012345678");
        assertThat(saved.getMessage()).isEqualTo("안내");
    }
}
