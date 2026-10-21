package dev.sogong_girls.mr_world.domain.tour.entity;

import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;
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
@Table(name = "hotel_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HotelOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false)
    private HotelGrade grade;

    @Column(nullable = false)
    private Integer price;

    private String description;
}
