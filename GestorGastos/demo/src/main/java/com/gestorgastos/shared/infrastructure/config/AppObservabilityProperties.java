package com.gestorgastos.shared.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.observability")
public class AppObservabilityProperties {
	@NotBlank
	private String traceHeaderName = "X-Trace-Id";
}
