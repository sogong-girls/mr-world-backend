package dev.sogong_girls.mr_world.domain.user.dto;

public record UserUpdateRequest(
    String name,
    String phoneNumber,
    String address
) {}
