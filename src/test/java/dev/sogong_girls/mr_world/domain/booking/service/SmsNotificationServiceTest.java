package dev.sogong_girls.mr_world.domain.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.booking.mapper.SmsNotificationMapper;
import dev.sogong_girls.mr_world.domain.booking.repository.SmsNotificationRepository;
import dev.sogong_girls.mr_world.domain.user.repository.UserRepository;
import dev.sogong_girls.mr_world.global.config.AppProperties;

class SmsNotificationServiceTest {

    @Test
    void keepsSendingRemainingNotificationsWhenOneFails() {
        SmsNotificationRepository repository = mock(SmsNotificationRepository.class);
        SmsNotification rejected = notification("rejected");
        SmsNotification broken = notification("broken");
        SmsNotification ok = notification("ok");
        given(repository.findUnsentForUnpaidBookings(TourStatus.CONFIRMED)).willReturn(List.of(rejected));
        given(repository.findAllBySentAtIsNullAndSendDueLessThanEqual(any())).willReturn(List.of(broken, ok));

        SmsNotificationService service = spy(new SmsNotificationService(repository,
                mock(UserRepository.class), mock(SmsNotificationMapper.class), mock(AppProperties.class)));
        willThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null))
                .given(service).sendNoticeDev(rejected);
        willThrow(new IllegalStateException("unexpected")).given(service).sendNoticeDev(broken);
        willReturn(true).given(service).sendNoticeDev(ok);

        service.checkPaymentNotifications();
        service.checkDepartureNotifications();

        then(service).should().sendNoticeDev(rejected);
        then(service).should().sendNoticeDev(broken);
        then(service).should().sendNoticeDev(ok);
    }

    private static SmsNotification notification(String message) {
        return SmsNotification.builder().bookingId(1L).phoneNumber("821012345678").message(message).build();
    }

    @Test
    void masksMiddleDigitsOfPhoneNumber() {
        assertThat(SmsNotificationService.maskPhoneNumber("821012345678")).isEqualTo("8210****5678");
    }

    @Test
    void leavesShortOrMissingPhoneNumberAsIs() {
        assertThat(SmsNotificationService.maskPhoneNumber(null)).isNull();
        assertThat(SmsNotificationService.maskPhoneNumber("1234")).isEqualTo("1234");
    }

    @Test
    void masksReceiverInMoceanResponseBody() {
        String body = """
                {"messages":[{"receiver":"821012345678","message_id":"CPASS_restapi_C0000002737000000.0001","status":0}]}
                """;

        assertThat(SmsNotificationService.maskReceivers(body))
                .contains("\"receiver\":\"8210****5678\"")
                .contains("CPASS_restapi_C0000002737000000.0001")
                .doesNotContain("821012345678");
    }

    @Test
    void leavesBodyWithoutReceiverUnchanged() {
        String body = "{\"status\":1,\"err_msg\":\"Authorization failed\"}";

        assertThat(SmsNotificationService.maskReceivers(body)).isEqualTo(body);
        assertThat(SmsNotificationService.maskReceivers(null)).isNull();
    }
}
