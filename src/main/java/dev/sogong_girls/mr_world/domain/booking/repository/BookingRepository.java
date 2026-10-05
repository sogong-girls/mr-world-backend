package dev.sogong_girls.mr_world.domain.booking.repository;

import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
