package dev.sogong_girls.mr_world.domain.user.dto;

public record LoginResponse(
    String accessToken,
    String tokenType
) {}
