package dev.sogong_girls.mr_world.domain.booking.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "sms_notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "send_due", nullable = false)
    private LocalDateTime sendDue;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    private String message;

    @Builder
    public SmsNotification(Long bookingId, LocalDateTime sendDue, LocalDateTime sentAt, String message) {
        this.bookingId = bookingId;
        this.sendDue = sendDue;
        this.sentAt = sentAt;
        this.message = message;
    }
}
