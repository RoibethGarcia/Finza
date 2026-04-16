package com.gestorgastos.reporting.application.service;

import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryView(
	LocalDate periodStart,
	LocalDate periodEnd,
	List<CurrencySummaryView> currencies,
	List<AccountBalanceView> accounts,
	List<RecentTransactionView> recentTransactions
) {
}
