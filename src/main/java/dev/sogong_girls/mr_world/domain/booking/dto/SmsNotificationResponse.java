package dev.sogong_girls.mr_world.domain.booking.dto;

import java.time.OffsetDateTime;

public record SmsNotificationResponse(
    Long id,
    Long bookingId,
    OffsetDateTime sendDue,
    OffsetDateTime sentAt,
    String message
) {}
