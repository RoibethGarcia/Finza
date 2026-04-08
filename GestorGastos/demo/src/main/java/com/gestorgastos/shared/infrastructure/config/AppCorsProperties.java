package com.gestorgastos.shared.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.cors")
public class AppCorsProperties {

	@NotNull
	private List<String> allowedOrigins = new ArrayList<>();
	@NotNull
	private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
	@NotNull
	private List<String> allowedHeaders = new ArrayList<>(List.of("Authorization", "Content-Type", "X-Trace-Id"));
	@NotNull
	private List<String> exposedHeaders = new ArrayList<>(List.of("X-Trace-Id"));
	private boolean allowCredentials = true;
	@NotNull
	private Duration maxAge = Duration.ofHours(1);
}
