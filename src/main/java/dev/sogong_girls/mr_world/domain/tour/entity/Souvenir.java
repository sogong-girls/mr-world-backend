package dev.sogong_girls.mr_world.domain.tour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "souvenir")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Souvenir {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;

    private String description;

    @Column(name = "stock_count", nullable = false)
    private Integer stockCount;
}
