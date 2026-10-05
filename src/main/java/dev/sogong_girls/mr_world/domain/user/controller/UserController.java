package dev.sogong_girls.mr_world.domain.user.controller;

import dev.sogong_girls.mr_world.domain.user.dto.UserResponse;
import dev.sogong_girls.mr_world.domain.user.dto.UserUpdateRequest;
import dev.sogong_girls.mr_world.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/api/users/me")
    public UserResponse getMe() {
        return service.getMe();
    }

    @PatchMapping("/api/users/me")
    public UserResponse updateMe(@Valid @RequestBody UserUpdateRequest request) {
        return service.updateMe(request);
    }
}
