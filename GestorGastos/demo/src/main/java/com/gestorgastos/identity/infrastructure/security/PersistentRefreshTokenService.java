package com.gestorgastos.identity.infrastructure.security;

import com.gestorgastos.identity.application.port.RefreshTokenStore;
import com.gestorgastos.identity.application.security.RefreshTokenService;
import com.gestorgastos.identity.domain.exception.RefreshTokenExpiredException;
import com.gestorgastos.identity.domain.exception.RefreshTokenNotFoundException;
import com.gestorgastos.identity.domain.exception.RefreshTokenRevokedException;
import com.gestorgastos.identity.domain.model.IssuedRefreshToken;
import com.gestorgastos.identity.domain.model.RefreshToken;
import com.gestorgastos.shared.infrastructure.config.AppSecurityProperties;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
public class PersistentRefreshTokenService implements RefreshTokenService {

	private final RefreshTokenStore refreshTokenStore;
	private final TokenHashingService tokenHashingService;
	private final AppSecurityProperties.Jwt properties;
	private final Clock clock;
	private final SecureRandom secureRandom = new SecureRandom();

	public PersistentRefreshTokenService(
			RefreshTokenStore refreshTokenStore,
			TokenHashingService tokenHashingService,
			AppSecurityProperties securityProperties,
			Clock clock
	) {
		this.refreshTokenStore = refreshTokenStore;
		this.tokenHashingService = tokenHashingService;
		this.properties = securityProperties.getJwt();
		this.clock = clock;
	}

	@Override
	public IssuedRefreshToken issue(UUID userId) {
		return issue(userId, UUID.randomUUID());
	}

	@Override
	public RefreshToken validate(String rawToken) {
		RefreshToken refreshToken = refreshTokenStore.findByTokenHash(tokenHashingService.hash(rawToken))
			.orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token was not found."));

		if (refreshToken.isRevoked()) {
			throw new RefreshTokenRevokedException("Refresh token has already been revoked.");
		}
		if (refreshToken.isExpired(clock)) {
			throw new RefreshTokenExpiredException("Refresh token has expired.");
		}

		return refreshToken;
	}

	@Override
	public IssuedRefreshToken rotate(String rawToken) {
		RefreshToken currentToken = validate(rawToken);
		Instant now = Instant.now(clock);
		refreshTokenStore.save(currentToken.revoke(now));
		return issue(currentToken.userId(), currentToken.familyId());
	}

	@Override
	public void revoke(String rawToken) {
		RefreshToken currentToken = validate(rawToken);
		refreshTokenStore.save(currentToken.revoke(Instant.now(clock)));
	}

	private IssuedRefreshToken issue(UUID userId, UUID familyId) {
		Instant issuedAt = Instant.now(clock);
		Instant expiresAt = issuedAt.plus(properties.getRefreshTokenTtl());
		UUID tokenId = UUID.randomUUID();
		String rawToken = generateRawToken();

		refreshTokenStore.save(new RefreshToken(
			tokenId,
			userId,
			familyId,
			tokenHashingService.hash(rawToken),
			issuedAt,
			expiresAt,
			null,
			issuedAt,
			issuedAt
		));

		return new IssuedRefreshToken(tokenId, familyId, userId, rawToken, expiresAt);
	}

	private String generateRawToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
