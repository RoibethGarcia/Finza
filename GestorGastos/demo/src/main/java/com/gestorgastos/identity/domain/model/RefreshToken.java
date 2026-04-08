package com.gestorgastos.identity.domain.model;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public record RefreshToken(
	UUID id,
	UUID userId,
	UUID familyId,
	String tokenHash,
	Instant issuedAt,
	Instant expiresAt,
	Instant revokedAt,
	Instant createdAt,
	Instant updatedAt
) {

	public boolean isExpired(Clock clock) {
		return expiresAt.isBefore(Instant.now(clock));
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

	public RefreshToken revoke(Instant revokedAt) {
		return new RefreshToken(id, userId, familyId, tokenHash, issuedAt, expiresAt, revokedAt, createdAt, updatedAt);
	}
}
