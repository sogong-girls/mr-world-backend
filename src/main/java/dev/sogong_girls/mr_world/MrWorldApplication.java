package dev.sogong_girls.mr_world;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// 스케줄링 설정은 global.config.SchedulingConfig에 있다
@SpringBootApplication
@ConfigurationPropertiesScan
public class MrWorldApplication {

	public static void main(String[] args) {
		SpringApplication.run(MrWorldApplication.class, args);
	}

}
