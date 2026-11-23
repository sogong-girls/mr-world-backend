package dev.sogong_girls.mr_world.domain.booking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;

public interface SmsNotificationRepository extends JpaRepository<SmsNotification, Long> {

    @Query("""
                SELECT s
                FROM SmsNotification s
                WHERE s.sentAt IS NULL
                AND s.sendDue IS NULL
                AND EXISTS (
                    SELECT b.id
                    FROM Booking b
                    WHERE b.id = s.bookingId
                    AND b.status IN :statuses
                )
            """)
    List<SmsNotification> findUnsentForUnpaidBookings(@Param("statuses") List<TourStatus> statuses);

    List<SmsNotification> findAllBySentAtIsNullAndSendDueLessThanEqual(LocalDateTime sendDue);

    /**
     * 전송 완료 시각만 기록한다. 엔티티 전체를 저장(merge)하면 조회 이후 바뀐 다른 컬럼(전화번호 등)을
     * 예전 값으로 덮어쓸 수 있어서 sent_at만 갱신한다.
     *
     * @return 갱신된 행 수. 이미 전송 처리된 알림이면 0
     */
    @Transactional
    @Modifying
    @Query("""
                UPDATE SmsNotification s
                SET s.sentAt = :sentAt
                WHERE s.id = :id
                AND s.sentAt IS NULL
            """)
    int markSent(@Param("id") Long id, @Param("sentAt") LocalDateTime sentAt);

    public List<SmsNotification> findAllByPhoneNumber(String phoneNumber);

    @Query("""
                SELECT s
                FROM SmsNotification s
                WHERE s.phoneNumber = :phoneNumber
                AND s.sentAt IS NULL
            """)
    public List<SmsNotification> findUnsentByPhoneNumber(@Param("phoneNumber") String phoneNumber);

}
