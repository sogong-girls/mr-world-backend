package dev.sogong_girls.mr_world.domain.booking.repository;

import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.entity.BookingAddon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingAddonRepository extends JpaRepository<BookingAddon, Long> {
    @EntityGraph(attributePaths = "addonOption")
    List<BookingAddon> findAllByBookingId(Long bookingId);
}
