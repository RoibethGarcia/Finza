package com.gestorgastos.reporting.application.service;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceView(
	UUID accountId,
	String accountName,
	String currency,
	BigDecimal openingBalance,
	BigDecimal currentBalance,
	BigDecimal periodIncome,
	BigDecimal periodExpense,
	BigDecimal periodNet
) {
}
