package dev.sogong_girls.mr_world.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties()
public record AppProperties(
                String DB_URL,
                String DB_USERNAME,
                String DB_PASSWORD,
                String SMS_API_KEY,
                String SMS_SENDER_ID,
                String SMS_SENDER_PHONE_NUMBER) {
}