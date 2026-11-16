package dev.sogong_girls.mr_world.domain.user.entity;

import dev.sogong_girls.mr_world.domain.user.enums.Role;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "mr_world_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(name = "refresh_token_hash", length = 64)
    private String refreshTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(name = "is_regular")
    private Boolean isRegular;

    @Builder
    public User(String loginId, String password, Role role, String name,
            String phoneNumber, String address) {
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.name = name;
        this.phoneNumber = formatPhoneNumber(phoneNumber);
        this.address = address;
        this.isRegular = false;
    }

    public void updateInfo(String name, String phoneNumber, String address) {
        if (name != null)
            this.name = name;
        if (phoneNumber != null)
            this.phoneNumber = formatPhoneNumber(phoneNumber);
        if (address != null)
            this.address = address;
    }

    public void updateIsRegular(Boolean isRegular) {
        if (isRegular != null)
            this.isRegular = isRegular;
    }

    public void updateRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    // 01012345678, 010-1234-5678 등 → 821012345678 형식으로 정규화
    private static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null)
            return null;
        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("010")) {
            return "82" + digits.substring(1);
        }
        throw new BusinessException(BusinessErrorCode.INVALID_INPUT, "전화번호 형식이 잘못되었습니다.");
    }
}
