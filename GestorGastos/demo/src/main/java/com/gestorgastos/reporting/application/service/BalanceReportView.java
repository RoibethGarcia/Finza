package com.gestorgastos.reporting.application.service;

import java.time.LocalDate;
import java.util.List;

public record BalanceReportView(
	LocalDate periodStart,
	LocalDate periodEnd,
	List<CurrencySummaryView> currencies,
	List<AccountBalanceView> accounts
) {
}
