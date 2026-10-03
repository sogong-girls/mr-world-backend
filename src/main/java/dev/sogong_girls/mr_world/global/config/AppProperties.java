package dev.sogong_girls.mr_world.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AppProperties: 앞으로 Properties 만들게 되면 기본 템플릿으로 사용
 * 추후 앱 내 실제 필요한 것(JWT Secret, 쿠키 expire 시간 등)만 가져오도록 수정 예정
 */
@ConfigurationProperties()
public record AppProperties(
        String DB_URL,
        String DB_USERNAME,
        String DB_PASSWORD) {
}