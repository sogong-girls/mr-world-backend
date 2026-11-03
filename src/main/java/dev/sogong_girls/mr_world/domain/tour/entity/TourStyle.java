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
import lombok.Builder;
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

    @Builder
    public TourStyle(Long tourId, TourStyleType type, Integer basePrice, Float regularDiscount,
            Long hotelOptionId, Long foodOptionId, String description) {
        this.tourId = tourId;
        this.type = type;
        this.basePrice = basePrice;
        this.regularDiscount = regularDiscount;
        this.hotelOptionId = hotelOptionId;
        this.foodOptionId = foodOptionId;
        this.description = description;
    }

    public void updateInfo(TourStyleType type, Integer basePrice, Float regularDiscount,
            Long hotelOptionId, Long foodOptionId, String description) {
        if (type != null)
            this.type = type;
        if (basePrice != null)
            this.basePrice = basePrice;
        if (regularDiscount != null)
            this.regularDiscount = regularDiscount;
        if (hotelOptionId != null)
            this.hotelOptionId = hotelOptionId;
        if (foodOptionId != null)
            this.foodOptionId = foodOptionId;
        if (description != null)
            this.description = description;
    }
}
