package dev.sogong_girls.mr_world.domain.booking.entity;

import java.time.LocalDateTime;
import java.util.List;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
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
    private LocalDateTime createdAt;

    @Builder
    public Booking(Tour tour, Long userId, Long tourStyleId, TourStyleType tourStyleType,
            HotelOption hotelOption, FoodOption foodOption,
            Integer totalPrice, Integer discountPrice, Integer participantsCount,
            LocalDateTime startAt, LocalDateTime endAt,
            Integer nights, Integer days, TourStatus status) {
        this.tour = tour;
        this.userId = userId;
        this.tourStyleId = tourStyleId;
        this.tourStyleType = tourStyleType;
        this.hotelOption = hotelOption;
        this.foodOption = foodOption;
        this.totalPrice = totalPrice;
        this.discountPrice = discountPrice;
        this.participantsCount = participantsCount;
        this.startAt = startAt;
        this.endAt = endAt;
        this.nights = nights;
        this.days = days;
        this.status = status;
        this.createdAt = LocalDateTime.now();

        if (status == TourStatus.CONFIRMED)
            this.paymentDueAt = createdAt.plusDays(1);
    }

    public void updateBookingAddons(List<BookingAddon> bookingAddons) {
        if (bookingAddons != null) {
            this.bookingAddons = bookingAddons;
        }
    }

    public void updateOptions(Long tourStyleId, TourStyleType tourStyleType,
            HotelOption hotelOption, FoodOption foodOption, Integer totalPrice, Integer discountPrice) {
        if (tourStyleId != null)
            this.tourStyleId = tourStyleId;
        if (tourStyleType != null)
            this.tourStyleType = tourStyleType;
        if (hotelOption != null)
            this.hotelOption = hotelOption;
        if (foodOption != null)
            this.foodOption = foodOption;
        if (totalPrice != null)
            this.totalPrice = totalPrice;
        if (discountPrice != null)
            this.discountPrice = discountPrice;
    }

    public void updateStatus(TourStatus status) {
        if (status != null) {
            this.status = status;

            if (status == TourStatus.CONFIRMED) {
                this.paymentDueAt = LocalDateTime.now().plusDays(1);
            }
        }
    }

    public void updatePaymentDueAt(LocalDateTime paymentDueAt) {
        if (paymentDueAt == null)
            this.paymentDueAt = LocalDateTime.now().plusDays(1);
        else
            this.paymentDueAt = paymentDueAt;
    }

    public void updatePaidAt() {
        if (this.paidAt == null) {
            this.paidAt = LocalDateTime.now();
            this.status = TourStatus.BOOKED;
        } else
            throw new BusinessException(BusinessErrorCode.ALREADY_PAID);
    }
}
