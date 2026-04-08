package com.gestorgastos.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

public record IssuedRefreshToken(UUID tokenId, UUID familyId, UUID userId, String rawToken, Instant expiresAt) {
}
