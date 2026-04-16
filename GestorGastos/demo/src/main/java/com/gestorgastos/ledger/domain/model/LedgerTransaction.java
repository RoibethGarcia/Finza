package com.gestorgastos.ledger.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerTransaction(
	UUID id,
	UUID userId,
	UUID accountId,
	UUID categoryId,
	TransactionType type,
	BigDecimal amount,
	String currency,
	Instant occurredAt,
	String description,
	String referenceType,
	UUID referenceId,
	UUID transferGroupId,
	Instant createdAt,
	Instant updatedAt
) {
}
