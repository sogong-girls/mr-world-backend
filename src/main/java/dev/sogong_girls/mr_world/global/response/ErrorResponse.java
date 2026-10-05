package dev.sogong_girls.mr_world.global.response;

public record ErrorResponse(
    Integer status,
    String message
) {}
