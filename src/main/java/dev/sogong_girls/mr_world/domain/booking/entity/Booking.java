package dev.sogong_girls.mr_world.domain.booking.entity;

import java.time.LocalDateTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "booking")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tour_style_id", nullable = false)
    private Long tourStyleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tour_style_type", nullable = false)
    private TourStyleType tourStyleType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_option_id", nullable = false)
    private HotelOption hotelOption;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_option_id", nullable = false)
    private FoodOption foodOption;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private List<BookingAddon> bookingAddons;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "discount_price")
    private Integer discountPrice;

    @Column(name = "payment_due_at", nullable = false)
    private LocalDateTime paymentDueAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "participants_count", nullable = false)
    private Integer participantsCount;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "n_nights")
    private Integer nights;

    @Column(name = "n_days")
    private Integer days;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TourStatus status;

    @Column(name = "created_at")
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public Booking(Tour tour, Long userId, Long tourStyleId, TourStyleType tourStyleType,
            HotelOption hotelOption, FoodOption foodOption, List<BookingAddon> bookingAddons,
            Integer totalPrice, Integer discountPrice, LocalDateTime paymentDueAt,
            LocalDateTime paidAt, Integer participantsCount, LocalDateTime startAt,
            LocalDateTime endAt, Integer nights, Integer days, TourStatus status) {
        this.tour = tour;
        this.userId = userId;
        this.tourStyleId = tourStyleId;
        this.tourStyleType = tourStyleType;
        this.hotelOption = hotelOption;
        this.foodOption = foodOption;
        this.bookingAddons = bookingAddons;
        this.totalPrice = totalPrice;
        this.discountPrice = discountPrice;
        this.paymentDueAt = paymentDueAt;
        this.paidAt = paidAt;
        this.participantsCount = participantsCount;
        this.startAt = startAt;
        this.endAt = endAt;
        this.nights = nights;
        this.days = days;
        this.status = status;
    }
}
