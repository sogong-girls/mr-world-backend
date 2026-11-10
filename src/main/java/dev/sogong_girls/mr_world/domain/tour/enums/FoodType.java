package dev.sogong_girls.mr_world.domain.tour.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoodType {
    LUNCHBOX(3),
    LOCAL_RESTAURANT(4),
    PREMIUM_RESTAURANT(5);

    private final int grade;
}
