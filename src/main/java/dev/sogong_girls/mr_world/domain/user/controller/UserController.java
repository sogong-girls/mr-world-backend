package dev.sogong_girls.mr_world.domain.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.user.dto.UserResponse;
import dev.sogong_girls.mr_world.domain.user.dto.UserUpdateRequest;
import dev.sogong_girls.mr_world.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/api/users/me")
    public UserResponse getUser() {
        return service.getUser();
    }

    @PatchMapping("/api/users/me")
    public UserResponse updateUser(@Valid @RequestBody UserUpdateRequest request) {
        return service.updateUser(request);
    }
}
