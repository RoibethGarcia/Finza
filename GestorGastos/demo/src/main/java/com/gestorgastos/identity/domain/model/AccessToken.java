package com.gestorgastos.identity.domain.model;

import java.time.Instant;

public record AccessToken(String value, Instant issuedAt, Instant expiresAt) {
}
