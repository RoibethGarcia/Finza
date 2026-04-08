package com.gestorgastos.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfiguration {

	@Bean
	public ZoneId applicationZoneId(AppTimeProperties properties) {
		return ZoneId.of(properties.getZoneId());
	}

	@Bean
	public Clock applicationClock(ZoneId applicationZoneId) {
		return Clock.system(applicationZoneId);
	}
}
