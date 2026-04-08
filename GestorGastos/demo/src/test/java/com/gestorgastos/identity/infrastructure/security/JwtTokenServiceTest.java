package com.gestorgastos.identity.infrastructure.security;

import com.gestorgastos.identity.domain.exception.InvalidAccessTokenException;
import com.gestorgastos.identity.domain.model.AuthenticatedUser;
import com.gestorgastos.shared.infrastructure.config.AppSecurityProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

	private final Clock fixedClock = Clock.fixed(Instant.parse("2026-04-07T12:00:00Z"), ZoneOffset.UTC);
	private final JwtTokenService tokenService = new JwtTokenService(securityProperties(), fixedClock);

	@Test
	void shouldIssueAndParseAccessToken() {
		AuthenticatedUser authenticatedUser = new AuthenticatedUser(UUID.randomUUID(), "user@example.com", Set.of("USER"));

		var accessToken = tokenService.issueAccessToken(authenticatedUser);
		var parsed = tokenService.parseAccessToken(accessToken.value());

		assertThat(accessToken.issuedAt()).isEqualTo(Instant.parse("2026-04-07T12:00:00Z"));
		assertThat(accessToken.expiresAt()).isEqualTo(Instant.parse("2026-04-07T12:15:00Z"));
		assertThat(parsed).isEqualTo(authenticatedUser);
	}

	@Test
	void shouldRejectMalformedAccessToken() {
		assertThatThrownBy(() -> tokenService.parseAccessToken("not-a-jwt"))
			.isInstanceOf(InvalidAccessTokenException.class);
	}

	private AppSecurityProperties securityProperties() {
		AppSecurityProperties properties = new AppSecurityProperties();
		properties.getJwt().setIssuer("gestor-gastos-api-test");
		properties.getJwt().setAudience("gestor-gastos-test-clients");
		properties.getJwt().setSecret("test-secret-at-least-32-characters-long");
		properties.getJwt().setAccessTokenTtl(java.time.Duration.ofMinutes(15));
		return properties;
	}
}
