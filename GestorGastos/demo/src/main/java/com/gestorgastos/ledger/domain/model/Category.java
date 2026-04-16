package com.gestorgastos.ledger.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Category(
	UUID id,
	UUID userId,
	String name,
	CategoryType type,
	boolean archived,
	Instant createdAt,
	Instant updatedAt
) {
}
