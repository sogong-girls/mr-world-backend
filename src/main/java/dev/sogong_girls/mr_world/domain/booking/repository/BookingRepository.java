package dev.sogong_girls.mr_world.domain.booking.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = { "tour", "hotelOption", "foodOption", "bookingAddons.addonOption" })
    Optional<Booking> findWithOptionsById(Long id);

    public List<Booking> findAllByUserId(Long userId);

    long countByUserIdAndStatusIn(Long userId, Collection<TourStatus> statuses);

    List<Booking> findAllByTourIdAndStartAtAndEndAtAndStatusOrderByCreatedAtAscIdAsc(
            Long tourId, LocalDateTime startAt, LocalDateTime endAt, TourStatus status, Pageable pageable);

    @Query("""
                SELECT b
                FROM Booking b
                JOIN FETCH b.tour t
                WHERE t.souvenirId IS NOT NULL
                AND b.status IN :statuses
                ORDER BY b.startAt ASC, b.id ASC
            """)
    List<Booking> findAllWithSouvenirByStatusIn(@Param("statuses") Collection<TourStatus> statuses);

}
