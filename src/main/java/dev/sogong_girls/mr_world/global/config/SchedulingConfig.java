package dev.sogong_girls.mr_world.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 정기 작업(@Scheduled)을 켠다. app.scheduling.enabled=false면 꺼진다.
 * 테스트에서는 꺼서, 테스트 데이터로 실제 SMS가 나가거나 예약 상태가 바뀌지 않게 한다.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
