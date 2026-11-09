package dev.sogong_girls.mr_world.domain.tour.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TourStyleType {
    CLASSIC(3),
    GRAND(4),
    PREMIUM(5);

    private final int grade;
}
