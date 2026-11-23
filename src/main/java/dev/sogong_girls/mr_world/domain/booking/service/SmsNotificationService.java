package dev.sogong_girls.mr_world.domain.booking.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotificationService {
    private static final String MOCEAN_SMS_URL = "https://rest.moceanapi.com/rest/2/sms";
    private static final Pattern RECEIVER_PATTERN = Pattern.compile("(\"receiver\"\\s*:\\s*\")([^\"]*)(\")");

    private final SmsNotificationRepository smsNotificationRepository;
    private final UserRepository userRepository;
    private final SmsNotificationMapper smsNotificationMapper;
    private final AppProperties properties;

    @Scheduled(cron = "0 * * * * *") // 1분마다 확인
    public void checkPaymentNotifications() {
        List<SmsNotification> targets = smsNotificationRepository
                .findUnsentForUnpaidBookings(List.of(TourStatus.CONFIRMED, TourStatus.BOOKED));

        int succeeded = 0;
        for (SmsNotification notification : targets) {
            // 한 건이 실패해도 나머지는 계속 보낸다. 실패한 건은 sentAt이 비어 있으니 다음 회차에 다시 시도된다
            try {
                if (sendNoticeDev(notification))
                    succeeded++;
            } catch (RestClientException e) {
                // 응답·실패 내용은 sendNoticeDev에서 이미 남겼다
            } catch (RuntimeException e) {
                log.error("[SMS] 전송 중 예상치 못한 오류 | notificationId={}", notification.getId(), e);
            }
        }

        if (!targets.isEmpty())
            log.info("[SMS] 결제 알림 정기 발송 완료 | 대상 {}건, 성공 {}건, 실패 {}건",
                    targets.size(), succeeded, targets.size() - succeeded);
    }

    @Scheduled(cron = "0 0 * * * *") // 1시간마다 확인
    public void checkDepartureNotifications() {
        List<SmsNotification> targets = smsNotificationRepository
                .findAllBySentAtIsNullAndSendDueLessThanEqual(LocalDateTime.now());

        int succeeded = 0;
        for (SmsNotification notification : targets) {
            // 한 건이 실패해도 나머지는 계속 보낸다. 실패한 건은 sentAt이 비어 있으니 다음 회차에 다시 시도된다
            try {
                if (sendNoticeDev(notification))
                    succeeded++;
            } catch (RestClientException e) {
                // 응답·실패 내용은 sendNoticeDev에서 이미 남겼다
            } catch (RuntimeException e) {
                log.error("[SMS] 전송 중 예상치 못한 오류 | notificationId={}", notification.getId(), e);
            }
        }

        if (!targets.isEmpty())
            log.info("[SMS] 출발 24시간 전 알림 정기 발송 완료 | 대상 {}건, 성공 {}건, 실패 {}건",
                    targets.size(), succeeded, targets.size() - succeeded);
    }

    @Transactional
    public List<SmsNotificationResponse> createNotifications(Booking booking) {

        User user = userRepository.findById(booking.getUserId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));

        String startAt = booking.getStartAt().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endAt = booking.getEndAt().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String tourName = booking.getTour().getName();

        List<SmsNotification> notifications = new ArrayList<>();
        notifications.add(SmsNotification.builder()
                .bookingId(booking.getId())
                .phoneNumber(user.getPhoneNumber())
                .message(String.format("%s~%s %s 예약이 확정되었습니다.%s", startAt, endAt, tourName,
                        booking.getStatus() == TourStatus.CONFIRMED ? " 결제를 진행해 주세요." : ""))
                .build());

        notifications.add(SmsNotification.builder()
                .bookingId(booking.getId())
                .phoneNumber(user.getPhoneNumber())
                .sendDue(booking.getStartAt().minusHours(24))
                .message(String.format("%s~%s %s 여행 출발 24시간 전 안내입니다.", startAt, endAt, tourName))
                .build());

        return smsNotificationMapper.toResponses(smsNotificationRepository.saveAll(notifications));
    }

    public void sendNotice() {
        throw new UnsupportedOperationException("sendNotice is not implemented yet");
    }

    /**
     * MOCEAN API로 SMS를 전송한다.
     *
     * @return Mocean이 202 ACCEPTED로 접수했으면 true
     * @throws RestClientException Mocean이 4xx·5xx로 응답했거나 응답을 받지 못한 경우
     */
    // 외부 HTTP 호출 동안 DB 커넥션을 잡지 않도록 트랜잭션을 걸지 않는다. sentAt은 markSent가 따로 커밋한다.
    public boolean sendNoticeDev(SmsNotification smsNotification) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("mocean-from", properties.SMS_SENDER_ID());
        body.add("mocean-to", smsNotification.getPhoneNumber());
        body.add("mocean-text", smsNotification.getMessage());

        // API 키는 남기지 않고, 전화번호는 가운데를 가려서 남긴다
        log.info("[SMS] request  -> POST {} | notificationId={}, bookingId={}, to={}, text=\"{}\"",
                MOCEAN_SMS_URL, smsNotification.getId(), smsNotification.getBookingId(),
                maskPhoneNumber(smsNotification.getPhoneNumber()), smsNotification.getMessage());

        long startedAt = System.nanoTime();
        ResponseEntity<String> response;
        try {
            response = RestClient.create()
                    .post()
                    .uri(MOCEAN_SMS_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.SMS_API_KEY().strip())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException e) {
            // Mocean이 4xx·5xx로 응답한 경우: 실패 사유가 응답 본문에 담겨 온다
            log.warn("[SMS] response <- {} ({}ms) | notificationId={} | body={}",
                    e.getStatusCode(), elapsedMs(startedAt), smsNotification.getId(),
                    maskReceivers(e.getResponseBodyAsString()));
            throw e;
        } catch (RestClientException e) {
            // 연결 실패·타임아웃 등 응답을 받지 못한 경우
            log.error("[SMS] failed   <- ({}ms) | notificationId={} | cause: {}",
                    elapsedMs(startedAt), smsNotification.getId(), e.toString());
            throw e;
        }

        log.info("[SMS] response <- {} ({}ms) | notificationId={} | body={}",
                response.getStatusCode(), elapsedMs(startedAt), smsNotification.getId(),
                maskReceivers(response.getBody()));

        if (response.getStatusCode() == HttpStatus.ACCEPTED) {
            smsNotificationRepository.markSent(smsNotification.getId(), LocalDateTime.now());
            return true;
        }
        return false;
    }

    private static long elapsedMs(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }

    /**
     * 821012345678 → 8210****5678
     */
    static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() <= 8)
            return phoneNumber;
        return phoneNumber.substring(0, 4)
                + "*".repeat(phoneNumber.length() - 8)
                + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * Mocean 응답 본문의 "receiver"(수신 번호)를 가린다.
     */
    static String maskReceivers(String responseBody) {
        if (responseBody == null)
            return null;
        return RECEIVER_PATTERN.matcher(responseBody)
                .replaceAll(match -> Matcher.quoteReplacement(
                        match.group(1) + maskPhoneNumber(match.group(2)) + match.group(3)));
    }
}
