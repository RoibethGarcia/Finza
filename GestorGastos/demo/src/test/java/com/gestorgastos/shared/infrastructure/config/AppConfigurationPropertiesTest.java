package com.gestorgastos.shared.infrastructure.config;

import com.gestorgastos.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AppConfigurationPropertiesTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private AppSecurityProperties securityProperties;

	@Autowired
	private AppCorsProperties corsProperties;

	@Autowired
	private AppTimeProperties timeProperties;

	@Autowired
	private AppObservabilityProperties observabilityProperties;

	@Test
	void shouldBindProfileSpecificProperties() {
		assertThat(securityProperties.getJwt().getIssuer()).isEqualTo("gestor-gastos-api-test");
		assertThat(securityProperties.getJwt().getAudience()).isEqualTo("gestor-gastos-test-clients");
		assertThat(securityProperties.getPassword().getBcryptStrength()).isEqualTo(4);
		assertThat(corsProperties.getAllowedOrigins()).containsExactly("http://localhost:4173");
		assertThat(timeProperties.getZoneId()).isEqualTo("UTC");
		assertThat(observabilityProperties.getTraceHeaderName()).isEqualTo("X-Trace-Id");
	}
}
