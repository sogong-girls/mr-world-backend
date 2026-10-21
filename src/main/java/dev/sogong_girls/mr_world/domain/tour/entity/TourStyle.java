package dev.sogong_girls.mr_world.domain.tour.entity;

import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tour_style")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourStyle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TourStyleType type;

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    @Column(name = "regular_discount")
    private Float regularDiscount;

    @Column(name = "hotel_option_id")
    private Long hotelOptionId;

    @Column(name = "food_option_id")
    private Long foodOptionId;

    private String description;
}
