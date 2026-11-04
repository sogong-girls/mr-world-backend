package dev.sogong_girls.mr_world.domain.user.service;

import dev.sogong_girls.mr_world.domain.user.dto.LoginRequest;
import dev.sogong_girls.mr_world.domain.user.dto.SignupRequest;
import dev.sogong_girls.mr_world.domain.user.dto.UserResponse;
import dev.sogong_girls.mr_world.domain.user.entity.User;
import dev.sogong_girls.mr_world.domain.user.enums.Role;
import dev.sogong_girls.mr_world.domain.user.mapper.UserMapper;
import dev.sogong_girls.mr_world.domain.user.repository.UserRepository;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import dev.sogong_girls.mr_world.global.security.JwtTokenProvider.JwtTokenPair;
import dev.sogong_girls.mr_world.global.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_ID);
        }
        // BCrypt accepts at most 72 bytes, including multi-byte characters.
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(BusinessErrorCode.INVALID_INPUT);
        }
        User user = User.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .build();
        return userMapper.toResponse(userRepository.save(user));
    }

    public JwtTokenPair login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.UNAUTHORIZED));
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }
        return issueTokens(user);
    }

    public JwtTokenPair refresh(String refreshToken) {
        Long userId;
        try {
            userId = tokenProvider.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.UNAUTHORIZED));
        if (!matchesRefreshToken(user, refreshToken)) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }
        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        Long userId;
        try {
            userId = tokenProvider.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            return;
        }
        userRepository.findByIdForUpdate(userId)
                .filter(user -> matchesRefreshToken(user, refreshToken))
                .ifPresent(user -> user.updateRefreshTokenHash(null));
    }

    private JwtTokenPair issueTokens(User user) {
        JwtTokenPair tokens = tokenProvider.createTokens(user.getId(), user.getRole());
        user.updateRefreshTokenHash(hash(tokens.refreshToken()));
        return tokens;
    }

    private boolean matchesRefreshToken(User user, String token) {
        return user.getRefreshTokenHash() != null
                && MessageDigest.isEqual(user.getRefreshTokenHash().getBytes(StandardCharsets.UTF_8),
                        hash(token).getBytes(StandardCharsets.UTF_8));
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
