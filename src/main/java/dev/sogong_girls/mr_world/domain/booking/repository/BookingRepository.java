package dev.sogong_girls.mr_world.domain.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.sogong_girls.mr_world.domain.booking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    public List<Booking> findAllByUserId(Long userId);

}
