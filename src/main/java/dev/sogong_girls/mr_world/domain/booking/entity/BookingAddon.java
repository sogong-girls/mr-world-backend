package dev.sogong_girls.mr_world.domain.booking.entity;

import dev.sogong_girls.mr_world.domain.booking.enums.Meal;
import dev.sogong_girls.mr_world.domain.tour.entity.AddonOption;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "booking_addon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingAddon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addon_option_id", nullable = false)
    private AddonOption addonOption;

    @Column(name = "nth_day")
    private Integer nthDay;

    @Enumerated(EnumType.STRING)
    private Meal meal;

    @Builder
    public BookingAddon(Long bookingId, AddonOption addonOption, Integer nthDay, Meal meal) {
        this.bookingId = bookingId;
        this.addonOption = addonOption;
        this.nthDay = nthDay;
        this.meal = meal;
    }

    public void updateInfo(AddonOption addonOption, Integer nthDay, Meal meal) {
        if (addonOption != null)
            this.addonOption = addonOption;
        if (nthDay != null)
            this.nthDay = nthDay;
        if (meal != null)
            this.meal = meal;
    }
}
