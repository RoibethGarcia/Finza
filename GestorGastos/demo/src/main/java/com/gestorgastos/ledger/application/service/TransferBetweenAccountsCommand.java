package com.gestorgastos.ledger.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferBetweenAccountsCommand(
	UUID sourceAccountId,
	UUID targetAccountId,
	BigDecimal amount,
	Instant occurredAt,
	String description
) {
}
