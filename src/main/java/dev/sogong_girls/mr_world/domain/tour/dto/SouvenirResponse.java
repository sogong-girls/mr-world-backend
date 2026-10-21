package dev.sogong_girls.mr_world.domain.tour.dto;

public record SouvenirResponse(
    Long id,
    String name,
    String brand,
    String description,
    Integer stockCount
) {}
