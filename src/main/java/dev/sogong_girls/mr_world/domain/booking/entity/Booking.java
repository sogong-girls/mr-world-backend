package dev.sogong_girls.mr_world.domain.booking.entity;

import java.time.LocalDateTime;

import dev.sogong_girls.mr_world.domain.booking.enums.TourStatus;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;
import dev.sogong_girls.mr_world.domain.user.entity.User;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_style_id", nullable = false)
    private TourStyle tourStyle;

    @Column(name = "hotel_option_id", nullable = false)
    private Long hotelOptionId;

    @Column(name = "food_option_id", nullable = false)
    private Long foodOptionId;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "discount_price")
    private Integer discountPrice;

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
    private TourStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
