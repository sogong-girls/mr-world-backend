package dev.sogong_girls.mr_world.domain.booking.repository;

import dev.sogong_girls.mr_world.domain.booking.entity.BookingAddon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingAddonRepository extends JpaRepository<BookingAddon, Long> {
}
