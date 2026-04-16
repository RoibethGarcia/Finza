package com.gestorgastos.reporting.application.service;

import java.math.BigDecimal;

public record CurrencySummaryView(
	String currency,
	BigDecimal totalIncome,
	BigDecimal totalExpense,
	BigDecimal netAmount,
	BigDecimal availableBalance
) {
}
