package dev.sogong_girls.mr_world.domain.booking.dto;

import java.time.LocalDateTime;

public record SmsNotificationResponse(
    Long id,
    Long bookingId,
    LocalDateTime sendDue,
    LocalDateTime sentAt,
    String message
) {}
