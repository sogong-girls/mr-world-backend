package dev.sogong_girls.mr_world.domain.tour.dto;

public record PriceResponse(
    Integer totalPrice,
    Integer discountPrice,
    Integer finalPrice
) {}
