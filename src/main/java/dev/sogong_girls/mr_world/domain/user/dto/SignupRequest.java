package dev.sogong_girls.mr_world.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
    @NotBlank String loginId,
    @NotBlank String password,
    @NotBlank String name,
    @NotBlank String phoneNumber,
    @NotBlank String address
) {}
