package com.gestorgastos.ledger.application.service;

import com.gestorgastos.ledger.domain.model.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountView(
	UUID id,
	String name,
	AccountType type,
	String currency,
	BigDecimal openingBalance,
	boolean archived,
	Instant createdAt,
	Instant updatedAt
) {
}
