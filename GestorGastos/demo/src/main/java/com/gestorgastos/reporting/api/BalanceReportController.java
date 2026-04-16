package com.gestorgastos.reporting.api;

import com.gestorgastos.reporting.application.service.AccountBalanceView;
import com.gestorgastos.reporting.application.service.BalanceReportView;
import com.gestorgastos.reporting.application.service.CurrencySummaryView;
import com.gestorgastos.reporting.application.service.DashboardSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports/balance")
public class BalanceReportController {

	private final DashboardSummaryService dashboardSummaryService;

	public BalanceReportController(DashboardSummaryService dashboardSummaryService) {
		this.dashboardSummaryService = dashboardSummaryService;
	}

	@GetMapping
	public BalanceReportResponse getBalanceReport(
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to
	) {
		BalanceReportView balanceReport = dashboardSummaryService.getBalanceReport(from, to);
		return new BalanceReportResponse(
			balanceReport.periodStart(),
			balanceReport.periodEnd(),
			balanceReport.currencies().stream()
				.map(summary -> new BalanceCurrencyResponse(
					summary.currency(),
					summary.totalIncome(),
					summary.totalExpense(),
					summary.netAmount(),
					summary.availableBalance()
				))
				.toList(),
			balanceReport.accounts().stream()
				.map(balance -> new BalanceAccountResponse(
					balance.accountId(),
					balance.accountName(),
					balance.currency(),
					balance.openingBalance(),
					balance.currentBalance(),
					balance.periodIncome(),
					balance.periodExpense(),
					balance.periodNet()
				))
				.toList()
		);
	}
}

record BalanceReportResponse(
	LocalDate periodStart,
	LocalDate periodEnd,
	List<BalanceCurrencyResponse> currencies,
	List<BalanceAccountResponse> accounts
) {
}

record BalanceCurrencyResponse(
	String currency,
	BigDecimal totalIncome,
	BigDecimal totalExpense,
	BigDecimal netAmount,
	BigDecimal availableBalance
) {
}

record BalanceAccountResponse(
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
