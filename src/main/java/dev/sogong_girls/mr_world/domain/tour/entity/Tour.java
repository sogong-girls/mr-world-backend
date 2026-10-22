package dev.sogong_girls.mr_world.domain.tour.entity;

import java.time.LocalTime;

import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
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
@Table(name = "tour")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String departure;

    @Column(nullable = false)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(name = "tour_theme", nullable = false)
    private TourTheme tourTheme;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false)
    private TransportType transportType;

    @Column(name = "souvenir_id", nullable = false)
    private Long souvenirId;

    @Column(name = "n_nights")
    private Integer nNights;

    @Column(name = "n_days")
    private Integer nDays;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    private String course;

    private String description;

    @Builder
    public Tour(String name, String departure, String destination, TourTheme tourTheme,
                TransportType transportType, Long souvenirId, Integer nNights, Integer nDays,
                LocalTime departureTime, LocalTime arrivalTime, String course, String description) {
        this.name = name;
        this.departure = departure;
        this.destination = destination;
        this.tourTheme = tourTheme;
        this.transportType = transportType;
        this.souvenirId = souvenirId;
        this.nNights = nNights;
        this.nDays = nDays;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.course = course;
        this.description = description;
    }
}
