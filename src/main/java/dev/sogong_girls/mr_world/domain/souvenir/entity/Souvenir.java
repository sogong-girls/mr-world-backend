package dev.sogong_girls.mr_world.domain.souvenir.entity;

import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
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

    public void updateInfo(String name, String brand, String description) {
        if (name != null)
            this.name = name;
        if (brand != null)
            this.brand = brand;
        if (description != null)
            this.description = description;
    }

    public void updateStockCount(Integer amount) {
        Integer result = this.stockCount + amount;
        if (result < 0)
            throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
        this.stockCount = result;
    }
}
