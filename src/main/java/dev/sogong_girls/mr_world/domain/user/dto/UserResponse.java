package dev.sogong_girls.mr_world.domain.user.dto;

import dev.sogong_girls.mr_world.domain.user.enums.Role;

public record UserResponse(
    Long id,
    String loginId,
    Role role,
    String name,
    String phoneNumber,
    String address,
    Boolean isRegular
) {}
