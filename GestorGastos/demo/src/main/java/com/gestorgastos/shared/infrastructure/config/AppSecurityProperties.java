package com.gestorgastos.shared.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

	@Valid
	@NotNull
	private Jwt jwt = new Jwt();

	@Valid
	@NotNull
	private Password password = new Password();

	@Getter
	@Setter
	public static class Jwt {
		@NotBlank
		private String issuer = "gestor-gastos-api";
		@NotBlank
		private String audience = "gestor-gastos-clients";
		@NotNull
		private Duration accessTokenTtl = Duration.ofMinutes(15);
		@NotNull
		private Duration refreshTokenTtl = Duration.ofDays(14);
		@NotBlank
		@Size(min = 32)
		private String secret = "change-me-local-secret-at-least-32-chars";
	}

	@Getter
	@Setter
	public static class Password {
		@Min(4)
		private int bcryptStrength = 12;
	}
}
