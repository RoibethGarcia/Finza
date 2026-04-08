package com.gestorgastos.identity.application.port;

import com.gestorgastos.identity.domain.model.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStore {

	RefreshToken save(RefreshToken refreshToken);
	Optional<RefreshToken> findByTokenHash(String tokenHash);
	Optional<RefreshToken> findByIdAndUserId(UUID tokenId, UUID userId);
	List<RefreshToken> findAllByUserId(UUID userId);
	void revokeActiveByUserId(UUID userId, Instant revokedAt);
}
