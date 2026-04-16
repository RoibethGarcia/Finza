package com.gestorgastos.reporting.application.service;

import com.gestorgastos.ledger.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecentTransactionView(
	UUID transactionId,
	UUID accountId,
	String accountName,
	UUID categoryId,
	String categoryName,
	TransactionType type,
	BigDecimal amount,
	String currency,
	Instant occurredAt,
	String description
) {
}
