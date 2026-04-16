package com.gestorgastos.ledger.application.service;

import com.gestorgastos.ledger.domain.model.TransactionType;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionFilters(
	UUID accountId,
	TransactionType type,
	LocalDate from,
	LocalDate to
) {
}
