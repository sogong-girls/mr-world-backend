package dev.sogong_girls.mr_world.domain.tour.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HotelGrade {
    _3_STAR(3),
    _4_STAR(4),
    _5_STAR(5);

    private final int grade;
}
