package com.gestorgastos.ledger.application.service;

import com.gestorgastos.ledger.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateTransactionCommand(
	UUID accountId,
	UUID categoryId,
	TransactionType type,
	BigDecimal amount,
	Instant occurredAt,
	String description
) {
}
