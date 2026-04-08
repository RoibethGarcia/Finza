package com.gestorgastos.identity.infrastructure.security;

import com.gestorgastos.identity.application.port.RefreshTokenStore;
import com.gestorgastos.identity.domain.exception.RefreshTokenRevokedException;
import com.gestorgastos.identity.domain.model.IssuedRefreshToken;
import com.gestorgastos.identity.domain.model.RefreshToken;
import com.gestorgastos.shared.infrastructure.config.AppSecurityProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistentRefreshTokenServiceTest {

	private final Clock fixedClock = Clock.fixed(Instant.parse("2026-04-07T12:00:00Z"), ZoneOffset.UTC);
	private final InMemoryRefreshTokenStore refreshTokenStore = new InMemoryRefreshTokenStore();
	private final Sha256TokenHashingService hashingService = new Sha256TokenHashingService();
	private final PersistentRefreshTokenService refreshTokenService = new PersistentRefreshTokenService(
		refreshTokenStore,
		hashingService,
		securityProperties(),
		fixedClock
	);

	@Test
	void shouldIssueValidateAndRotateRefreshTokens() {
		UUID userId = UUID.randomUUID();
		IssuedRefreshToken issued = refreshTokenService.issue(userId);
		RefreshToken validated = refreshTokenService.validate(issued.rawToken());
		IssuedRefreshToken rotated = refreshTokenService.rotate(issued.rawToken());

		assertThat(validated.userId()).isEqualTo(userId);
		assertThat(rotated.userId()).isEqualTo(userId);
		assertThat(rotated.familyId()).isEqualTo(issued.familyId());
		assertThat(rotated.rawToken()).isNotEqualTo(issued.rawToken());
		assertThatThrownBy(() -> refreshTokenService.validate(issued.rawToken()))
			.isInstanceOf(RefreshTokenRevokedException.class);
	}

	private AppSecurityProperties securityProperties() {
		AppSecurityProperties properties = new AppSecurityProperties();
		properties.getJwt().setRefreshTokenTtl(Duration.ofDays(14));
		properties.getJwt().setSecret("test-secret-at-least-32-characters-long");
		return properties;
	}

	private static class InMemoryRefreshTokenStore implements RefreshTokenStore {

		private final Map<UUID, RefreshToken> tokens = new LinkedHashMap<>();

		@Override
		public RefreshToken save(RefreshToken refreshToken) {
			tokens.put(refreshToken.id(), refreshToken);
			return refreshToken;
		}

		@Override
		public Optional<RefreshToken> findByTokenHash(String tokenHash) {
			return tokens.values().stream().filter(token -> token.tokenHash().equals(tokenHash)).findFirst();
		}

		@Override
		public Optional<RefreshToken> findByIdAndUserId(UUID tokenId, UUID userId) {
			return Optional.ofNullable(tokens.get(tokenId)).filter(token -> token.userId().equals(userId));
		}

		@Override
		public List<RefreshToken> findAllByUserId(UUID userId) {
			List<RefreshToken> results = new ArrayList<>();
			for (RefreshToken token : tokens.values()) {
				if (token.userId().equals(userId)) {
					results.add(token);
				}
			}
			return results;
		}

		@Override
		public void revokeActiveByUserId(UUID userId, Instant revokedAt) {
			tokens.replaceAll((id, token) -> token.userId().equals(userId) && !token.isRevoked() ? token.revoke(revokedAt) : token);
		}
	}
}
