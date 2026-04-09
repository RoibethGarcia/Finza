package com.gestorgastos.ledger.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Account(
	UUID id,
	UUID userId,
	String name,
	AccountType type,
	String currency,
	BigDecimal openingBalance,
	boolean archived,
	Instant createdAt,
	Instant updatedAt
) {
}
