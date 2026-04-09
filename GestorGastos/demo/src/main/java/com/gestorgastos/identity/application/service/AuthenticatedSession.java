package com.gestorgastos.identity.application.service;

import java.time.Instant;

public record AuthenticatedSession(
	UserProfile user,
	String accessToken,
	Instant accessTokenExpiresAt,
	String refreshToken,
	Instant refreshTokenExpiresAt
) {
}
