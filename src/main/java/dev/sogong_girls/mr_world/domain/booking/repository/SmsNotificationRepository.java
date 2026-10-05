package dev.sogong_girls.mr_world.domain.booking.repository;

import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsNotificationRepository extends JpaRepository<SmsNotification, Long> {
}
