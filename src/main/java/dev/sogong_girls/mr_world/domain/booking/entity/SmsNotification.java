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

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "send_due")
    private LocalDateTime sendDue;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    private String message;

    @Builder
    public SmsNotification(Long bookingId, String phoneNumber, LocalDateTime sendDue, String message) {
        this.bookingId = bookingId;
        this.phoneNumber = phoneNumber;
        this.sendDue = sendDue;
        this.sentAt = null;
        this.message = message;
    }

    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void updateSentAt() {
        this.sentAt = LocalDateTime.now();
    }
}
