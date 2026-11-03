package dev.sogong_girls.mr_world.domain.tour.entity;

import dev.sogong_girls.mr_world.domain.tour.enums.AddonType;
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
@Table(name = "addon_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddonOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AddonType type;

    @Column(nullable = false)
    private Integer price;

    private String description;

    @Builder
    public AddonOption(Long tourId, AddonType type, Integer price, String description) {
        this.tourId = tourId;
        this.type = type;
        this.price = price;
        this.description = description;
    }

    public void updateInfo(AddonType type, Integer price, String description) {
        if (type != null)
            this.type = type;
        if (price != null)
            this.price = price;
        if (description != null)
            this.description = description;
    }
}
