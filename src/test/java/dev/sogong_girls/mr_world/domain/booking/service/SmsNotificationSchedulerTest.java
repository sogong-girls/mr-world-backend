package dev.sogong_girls.mr_world.domain.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.spy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.test.context.ActiveProfiles;

import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.booking.mapper.SmsNotificationMapper;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingRepository;
import dev.sogong_girls.mr_world.domain.booking.repository.SmsNotificationRepository;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.enums.FoodType;
import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
import dev.sogong_girls.mr_world.domain.tour.repository.FoodOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.HotelOptionRepository;
import dev.sogong_girls.mr_world.domain.tour.repository.TourRepository;
import dev.sogong_girls.mr_world.domain.user.entity.User;
import dev.sogong_girls.mr_world.domain.user.enums.Role;
import dev.sogong_girls.mr_world.domain.user.repository.UserRepository;
import dev.sogong_girls.mr_world.global.config.AppProperties;

/**
 * SMS 정기 발송이 의도대로 동작하는지 실제 DB(H2)로 확인한다.
 * <ul>
 * <li>결제 알림: 1분마다, 아직 안 보냈고 CONFIRMED(미결제)인 예약의 결제 알림을 한 번 보낸다</li>
 * <li>출발 알림: 1시간마다, sendDue가 지난 미발송 알림을 한 번 보낸다</li>
 * </ul>
 * 스케줄러처럼 트랜잭션 밖에서 호출하려고 @Transactional을 걸지 않는다. 외부 Mocean 호출만 "202 접수"로 대신하고,
 * 그때도 실제 sentAt 기록(markSent)은 그대로 수행한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class SmsNotificationSchedulerTest {
    // 메시지 문구가 바뀌어도 테스트가 깨지지 않도록 종류로 구분한다 (결제 알림만 sendDue가 없다)
    private static final String PAYMENT = "PAYMENT";
    private static final String DEPARTURE = "DEPARTURE";

    @Autowired
    SmsNotificationRepository smsNotificationRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TourRepository tourRepository;
    @Autowired
    HotelOptionRepository hotelOptionRepository;
    @Autowired
    FoodOptionRepository foodOptionRepository;
    @Autowired
    SmsNotificationMapper smsNotificationMapper;
    @Autowired
    AppProperties appProperties;

    SmsNotificationService service;
    /** 발송된 문자: "bookingId:종류" */
    List<String> sent = new ArrayList<>();

    Tour tour;
    HotelOption hotel;
    FoodOption food;
    User user;

    @BeforeEach
    void setUp() {
        service = spy(new SmsNotificationService(smsNotificationRepository, userRepository,
                smsNotificationMapper, appProperties));
        willAnswer(invocation -> {
            SmsNotification notification = invocation.getArgument(0);
            sent.add(notification.getBookingId() + ":" + (notification.getSendDue() == null ? PAYMENT : DEPARTURE));
            smsNotificationRepository.markSent(notification.getId(), LocalDateTime.now());
            return true;
        }).given(service).sendNoticeDev(any());

        tour = tourRepository.save(Tour.builder().name("Test tour").departure("Seoul").destination("Busan")
                .tourTheme(TourTheme.TREKKING).transportType(TransportType.PREMIUM_CAR).build());
        hotel = hotelOptionRepository.save(HotelOption.builder().tourId(tour.getId()).name("Hotel")
                .location("Busan").grade(HotelGrade._3_STAR).price(100000).build());
        food = foodOptionRepository.save(FoodOption.builder().tourId(tour.getId()).name("Food")
                .type(FoodType.LUNCHBOX).price(10000).build());
        user = userRepository.save(User.builder().loginId("sms-tester").password("pw").role(Role.USER)
                .name("Tester").phoneNumber("010-0000-0000").address("Seoul").build());
    }

    @AfterEach
    void cleanUp() {
        smsNotificationRepository.deleteAll();
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        foodOptionRepository.deleteAll();
        hotelOptionRepository.deleteAll();
        tourRepository.deleteAll();
    }

    // ---------- 1. 결제 알림 (checkPaymentNotifications) ----------

    @Test
    void paymentCheckRunsEveryMinute() throws Exception {
        assertThat(intervalOf("checkPaymentNotifications")).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void sendsOnlyPaymentNoticeForConfirmedUnpaidBooking() {
        Booking booking = bookingWithNotifications(TourStatus.CONFIRMED, LocalDateTime.now().plusDays(10));

        service.checkPaymentNotifications();

        // 출발 24시간 전 안내는 아직 보낼 때가 아니므로 나가면 안 된다
        assertThat(sent).containsExactly(booking.getId() + ":" + PAYMENT);
    }

    @Test
    void doesNotResendPaymentNoticeOnNextMinute() {
        bookingWithNotifications(TourStatus.CONFIRMED, LocalDateTime.now().plusDays(10));

        service.checkPaymentNotifications();
        int sentAfterFirstRun = sent.size();
        service.checkPaymentNotifications();

        assertThat(sent).hasSize(sentAfterFirstRun);
    }

    @Test
    void sendsPaymentNoticeOnlyAfterPendingBookingIsConfirmed() {
        Booking booking = bookingWithNotifications(TourStatus.PENDING, LocalDateTime.now().plusDays(10));

        service.checkPaymentNotifications();
        assertThat(sent).isEmpty();

        booking.updateStatus(TourStatus.CONFIRMED);
        bookingRepository.save(booking);
        service.checkPaymentNotifications();

        assertThat(sent).contains(booking.getId() + ":" + PAYMENT);
    }

    @Test
    void doesNotSendPaymentNoticeForPaidBooking() {
        Booking booking = bookingWithNotifications(TourStatus.CONFIRMED, LocalDateTime.now().plusDays(10));
        booking.updatePaidAt();
        bookingRepository.save(booking);

        service.checkPaymentNotifications();

        assertThat(sent).isEmpty();
    }

    // ---------- 2. 출발 알림 (checkDepartureNotifications) ----------

    @Test
    void departureCheckRunsEveryHour() throws Exception {
        assertThat(intervalOf("checkDepartureNotifications")).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void sendsDepartureNoticeOnlyWhenSendDueHasPassed() {
        // 12시간 뒤 출발 → sendDue(출발 24시간 전)는 이미 지남
        Booking soon = bookingWithNotifications(TourStatus.BOOKED, LocalDateTime.now().plusHours(12));
        // 10일 뒤 출발 → sendDue가 아직 안 됨
        bookingWithNotifications(TourStatus.BOOKED, LocalDateTime.now().plusDays(10));

        service.checkDepartureNotifications();

        // 결제 알림(sendDue 없음)은 출발 알림 작업에서 나가면 안 된다
        assertThat(sent).containsExactly(soon.getId() + ":" + DEPARTURE);
    }

    // ---------- 메시지 내용 ----------

    @Test
    void paymentMessageShowsTourDatesAsYyyyMmDd() {
        // 연말에 출발해 해를 넘기는 투어: 주 기준 연도(YYYY) 같은 형식 오류가 있으면 연도가 틀어진다
        Booking booking = bookingWithNotifications(TourStatus.CONFIRMED, LocalDateTime.of(2026, 12, 30, 9, 0));

        assertThat(smsNotificationRepository.findAll())
                .filteredOn(notification -> notification.getBookingId().equals(booking.getId())
                        && notification.getSendDue() == null)
                .singleElement()
                .extracting(SmsNotification::getMessage)
                .isEqualTo("2026-12-30~2027-01-01 Test tour 예약이 확정되었습니다. 결제를 진행해 주세요.");
    }

    @Test
    void doesNotResendDepartureNoticeOnNextHour() {
        bookingWithNotifications(TourStatus.BOOKED, LocalDateTime.now().plusHours(12));

        service.checkDepartureNotifications();
        service.checkDepartureNotifications();

        assertThat(sent).hasSize(1);
    }

    // ---------- helpers ----------

    /** 예약을 만들고, 실제 예약 생성 흐름처럼 createNotifications로 알림 2건(결제·출발)을 등록한다 */
    private Booking bookingWithNotifications(TourStatus status, LocalDateTime startAt) {
        Booking booking = bookingRepository.save(Booking.builder().tour(tour).userId(user.getId()).tourStyleId(1L)
                .tourStyleType(TourStyleType.CLASSIC).hotelOption(hotel).foodOption(food)
                .totalPrice(100000).participantsCount(1)
                .startAt(startAt).endAt(startAt.plusDays(2)).status(status).build());
        service.createNotifications(booking);
        return booking;
    }

    private static Duration intervalOf(String methodName) throws NoSuchMethodException {
        Scheduled scheduled = SmsNotificationService.class.getMethod(methodName).getAnnotation(Scheduled.class);
        CronExpression cron = CronExpression.parse(scheduled.cron());
        LocalDateTime first = cron.next(LocalDateTime.of(2026, 11, 20, 10, 30, 15));
        return Duration.between(first, cron.next(first));
    }
}
