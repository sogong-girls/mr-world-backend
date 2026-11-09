package dev.sogong_girls.mr_world.domain.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.sogong_girls.mr_world.domain.booking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = { "tour", "hotelOption", "foodOption", "bookingAddons.addonOption" })
    Optional<Booking> findWithOptionsById(Long id);

    public List<Booking> findAllByUserId(Long userId);

}
