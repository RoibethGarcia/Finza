package com.gestorgastos.ledger.application.service;

import com.gestorgastos.ledger.domain.model.AccountType;

import java.math.BigDecimal;

public record CreateAccountCommand(
	String name,
	AccountType type,
	String currency,
	BigDecimal openingBalance
) {
}
