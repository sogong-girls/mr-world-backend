package dev.sogong_girls.mr_world.domain.booking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;

public interface SmsNotificationRepository extends JpaRepository<SmsNotification, Long> {

    @Query("""
                SELECT s
                FROM SmsNotification s
                WHERE s.sentAt IS NULL
                AND EXISTS (
                    SELECT b.id
                    FROM Booking b
                    WHERE b.id = s.bookingId
                    AND b.status = :status
                    AND b.paidAt IS NULL
                )
            """)
    List<SmsNotification> findUnsentForUnpaidBookings(@Param("status") TourStatus status);

    List<SmsNotification> findAllBySentAtIsNullAndSendDueLessThanEqual(LocalDateTime sendDue);

    public List<SmsNotification> findAllByPhoneNumber(String phoneNumber);

    @Query("""
                SELECT s
                FROM SmsNotification s
                WHERE s.phoneNumber = :phoneNumber
                AND s.sentAt = NULL
            """)
    public List<SmsNotification> findUnsentByPhoneNumber(@Param("phoneNumber") String phoneNumber);

}
