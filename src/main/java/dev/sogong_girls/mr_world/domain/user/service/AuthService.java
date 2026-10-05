package dev.sogong_girls.mr_world.domain.user.service;

import dev.sogong_girls.mr_world.domain.user.dto.LoginRequest;
import dev.sogong_girls.mr_world.domain.user.dto.LoginResponse;
import dev.sogong_girls.mr_world.domain.user.dto.SignupRequest;
import dev.sogong_girls.mr_world.domain.user.dto.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public UserResponse signup(SignupRequest request) {
        throw new UnsupportedOperationException("signup is not implemented yet");
    }

    public LoginResponse login(LoginRequest request) {
        throw new UnsupportedOperationException("login is not implemented yet");
    }

    public void logout() {
        throw new UnsupportedOperationException("logout is not implemented yet");
    }
}
